package eu.daiad.web.mapreduce;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

/**
 * Submits a map reduce job to a Hadoop YARN cluster.
 */
public class RunJar {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(RunJar.class);

    /**
     * Environment specific temporary directory.
     */
    private static final String PROPERTY_TMP_DIRECTORY = "java.io.tmpdir";

    /**
     * Pattern for matching files in JAR archive.
     */
    private static final Pattern MATCH_ANY = Pattern.compile(".*");

    /**
     * Ensures that a key exists in the properties.
     *
     * @param properties the properties to check.
     * @param key the key to find.
     * @param message error message.
     * @return true if the value is not null or empty.
     * @throws Exception if no value is assigned to the given key.
     */
    private boolean ensureParameter(Map<String, String> properties, String key, String message) throws Exception {
        return ensureParameter(properties, key, message, true);
    }

    /**
     * Ensures that a key exists in the properties.
     *
     * @param properties the properties to check.
     * @param key the key to find.
     * @param message error message.
     * @param throwException throw an exception if the parameter does not exist.
     * @return true if the value is not null or empty.
     * @throws Exception if no value is assigned to the given key.
     */
    private boolean ensureParameter(Map<String, String> properties, String key, String message, boolean throwException) throws Exception {
        if (!properties.containsKey(key)) {
            if (throwException) {
                throw new Exception(String.format("%s. Parameter: %s", message, key));
            }
            return false;
        }

        if (StringUtils.isBlank(properties.get(key))) {
            if (throwException) {
                throw new Exception(String.format("%s. Parameter: %s", message, key));
            }
            return false;
        }

        return true;
    }

    /**
     * Executes a map reduce job from an external jar file.
     *
     * @param properties properties to add to the job configuration.
     * @throws Throwable if the job execution fails.
     */
    public void run(Map<String, String> properties) throws Throwable {
        ensureParameter(properties, EnumJobMapReduceParameter.JOB_NAME.getValue(), "Job name is not set");
        ensureParameter(properties, EnumJobMapReduceParameter.JAR_NAME.getValue(), "JAR is not set");

        // Get JAR file
        String jarFilename = properties.get(EnumJobMapReduceParameter.JAR_NAME.getValue());

        File file = new File(jarFilename);
        if (!file.exists() || !file.isFile()) {
            throw new Exception(String.format("Not a valid JAR: %s", file.getCanonicalPath()));
        }

        JarFile jarFile;
        try {
            jarFile = new JarFile(jarFilename);
        } catch (IOException io) {
            throw new IOException(String.format("Error opening job jar: %s", jarFilename)).initCause(io);
        }

        // Select Main class
        String mainClassName = null;

        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
            mainClassName = manifest.getMainAttributes().getValue("Main-Class");
        }
        jarFile.close();

        if (mainClassName == null) {
            ensureParameter(properties, EnumJobMapReduceParameter.MAIN_CLASS_NAME.getValue(), "Main Class is not set");

            mainClassName = properties.get(EnumJobMapReduceParameter.MAIN_CLASS_NAME.getValue());
        }
        mainClassName = mainClassName.replaceAll("/", ".");

        // Create working directory
        String tmpDirPath = properties.get(EnumJobMapReduceParameter.LOCAL_TMP_PATH.getValue());
        if (StringUtils.isBlank(tmpDirPath)) {
            tmpDirPath = System.getProperty(PROPERTY_TMP_DIRECTORY);
        }
        File tmpDir = new File(tmpDirPath);
        ensureDirectory(tmpDir);

        final File workDir;
        try {
            workDir = File.createTempFile("yarn-mapreduce-", "", tmpDir);
        } catch (IOException ioe) {
            throw new Exception(String.format("Error creating temp dir in java.io.tmpdir %s due to %s.", tmpDir, ioe.getMessage()));
        }

        if (!workDir.delete()) {
            throw new Exception("Delete failed for " + workDir);
        }
        ensureDirectory(workDir);

        // Extract all files to the working directory
        unJar(file, workDir);

        // Get external libraries directory
        File libDir = null;
        if (ensureParameter(properties, EnumJobMapReduceParameter.LOCAL_LIB_PATH.getValue(), "Lib path is not set", false)) {
            libDir = new File(properties.get(EnumJobMapReduceParameter.LOCAL_LIB_PATH.getValue()));

            ensureDirectory(libDir);
        }

        // Copy job files
        copyLocalFilesToHdfs(properties);

        // Create class loader and submit job
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        URLClassLoader loader = null;
        Class<?> mainClass = null;
        Method main = null;

        try {
            // Set arguments
            List<String> arguments = new ArrayList<>();

            for (String key : properties.keySet()) {
                switch (EnumJobMapReduceParameter.fromString(key)) {
                    case JAR_NAME:
                        // Ignore JAR file name
                        break;
                    case MAIN_CLASS_NAME:
                        // Ignore Main Class name
                        break;
                    default:
                        arguments.add(key + "=" + properties.get(key));
                        break;
                }
            }

            // Initialize class loader and execute application
            loader = createClassLoader(file, libDir, workDir);
            Thread.currentThread().setContextClassLoader(loader);

            logCommand(jarFilename, mainClassName, arguments);

            // Invoke job
            mainClass = Class.forName(mainClassName, true, loader);
            main = mainClass.getMethod("main", new Class[] { Array.newInstance(String.class, 0).getClass() });
            main.invoke(null, new Object[] { arguments.toArray(new String[0]) });
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            // Reset class loader
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            // Delete temporary files and release resources
            FileUtils.deleteQuietly(workDir);

            main = null;
            mainClass = null;
            if (loader != null) {
                loader.close();
            }
            loader = null;

            Runtime.getRuntime().gc();
        }

        // TODO : Cleanup working directory (both local and HDFS one)
    }

    /**
     * Extract JAR file.
     *
     * @param jarFile the jar file.
     * @param toDir the path where to extract files.
     * @throws IOException if an I/O exception occurs.
     */
    private void unJar(File jarFile, File toDir) throws IOException {
        unJar(jarFile, toDir, MATCH_ANY);
    }

    /**
     * Extract JAR file.
     *
     * @param jarFile the jar file.
     * @param toDir the path where to extract files.
     * @param unpackRegex pattern for selecting files to extract.
     * @throws IOException if an I/O exception occurs.
     */
    private void unJar(File jarFile, File toDir, Pattern unpackRegex) throws IOException {
        JarFile jar = new JarFile(jarFile);
        try {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && unpackRegex.matcher(entry.getName()).matches()) {
                    InputStream in = jar.getInputStream(entry);
                    try {
                        File file = new File(toDir, entry.getName());
                        ensureDirectory(file.getParentFile());
                        OutputStream out = new FileOutputStream(file);
                        try {
                            IOUtils.copyBytes(in, out, 8192);
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                }
            }
        } finally {
            jar.close();
        }
    }

    /**
     * Creates a directory.
     *
     * @param dir the directory to create.
     * @throws IOException if an I/O exception occurs.
     */
    private void ensureDirectory(File dir) throws IOException {
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("Mkdirs failed to create " + dir.toString());
        }
    }

    /**
     * Creates a class loader. The application jar as well as the contents of
     * the working directory are added to the classpath. If external libraries
     * are declared, they are copied to the working directory and added to the
     * classpath too.
     *
     * @param file the job jar file.
     * @param libDir the local path with external jar files to add to the
     *               classpath.
     * @param workDir the currently working directory.
     * @return the initialized {@link ClassLoader}.
     *
     * @throws MalformedURLException if a path is malformed.
     * @throws IOException if an I/O operation fails.
     */
    private URLClassLoader createClassLoader(File file, final File libDir, final File workDir) throws MalformedURLException, IOException {
        List<URL> classPath = new ArrayList<URL>();

        classPath.add(file.toURI().toURL());

        classPath.add(new File(workDir + "/").toURI().toURL());
        classPath.add(new File(workDir, "classes/").toURI().toURL());

        if (libDir != null) {
            transferJarsToWorkingDir(libDir, new File(workDir, "lib"));

            File[] libs = new File(workDir, "lib").listFiles();
            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    classPath.add(libs[i].toURI().toURL());
                }
            }
        }

        return new URLClassLoader(classPath.toArray(new URL[0]));
    }

    /**
     * Copies the contents of a directory to another one.
     *
     * @param source the source directory.
     * @param target the target directory.
     * @throws IOException if an I/O exception occurs.
     */
    private void transferJarsToWorkingDir(File source, File target) throws IOException {
        try {
            FileUtils.copyDirectory(source, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copies local files to a temporary HDFS path. Two sets of files are
     * optionally copied (a) files that are available at HDFS and (b) files that
     * are available at HDFS but also cached on every YARN node and accessible
     * using symbolic links.
     *
     *  the job configuration properties.
     * @throws IOException if an I/O exception occurs.
     */
    private void copyLocalFilesToHdfs(Map<String, String> properties) throws IOException {
        // Create random base path
        String basePath = Paths.get(properties.get(EnumJobMapReduceParameter.HDFS_TMP_PATH.getValue()),
                                    RandomStringUtils.randomAlphanumeric(8)).toString();

        // Update temporary HDFS path
        properties.put(EnumJobMapReduceParameter.HDFS_TMP_PATH.getValue(), basePath);

        // Copy files to HDFS. These files are accessible from HDFS.
        String localFilePath = properties.get(EnumJobMapReduceParameter.LOCAL_FILE_PATH.getValue());

        String hdfsFilePath = properties.get(EnumJobMapReduceParameter.HDFS_FILE_PATH.getValue());
        if (StringUtils.isBlank(hdfsFilePath)) {
            hdfsFilePath = Paths.get(basePath, "files").toString();
        }

        copyFilesToHdfs(properties, localFilePath, hdfsFilePath);

        // Update parameter of the HDFS path in order to include the random base path.
        properties.put(EnumJobMapReduceParameter.HDFS_FILE_PATH.getValue(), hdfsFilePath);

        // Copy files from cache to HDFS. These files will be accessible locally on every YARN node.
        String localCachePath = properties.get(EnumJobMapReduceParameter.LOCAL_CACHE_PATH.getValue());

        String hdfsCachePath = properties.get(EnumJobMapReduceParameter.HDFS_CACHE_PATH.getValue());
        if (StringUtils.isBlank(hdfsCachePath)) {
            hdfsCachePath = Paths.get(basePath, "cache").toString();
        }

        copyFilesToHdfs(properties, localCachePath, hdfsCachePath);

        // Update parameter of the HDFS path in order to include the random base path.
        properties.put(EnumJobMapReduceParameter.HDFS_CACHE_PATH.getValue(), hdfsCachePath);
    }

    private void copyFilesToHdfs(Map<String, String> properties, String source, String target) throws IOException {
        if (!StringUtils.isBlank(source)) {
            Set<File> files = collectFilesFromLocalDir(source);

            if (!files.isEmpty()) {
                System.err.println(String.format("Copying [%d] files from local dir [%s] to HDFS dir [%s] at [%s]",
                                                 files.size(),
                                                 source,
                                                 target,
                                                 properties.get(EnumHadoopParameter.HDFS_PATH.getValue())));

                Configuration conf = new Configuration();
                conf.set(EnumHadoopParameter.HDFS_PATH.getValue(), properties.get(EnumHadoopParameter.HDFS_PATH.getValue()));

                FileSystem hdfsFileSystem = FileSystem.get(conf);

                for (File file : files) {
                    Path localJarPath = new Path(file.toURI());
                    Path hdfsJarPath = new Path(target, file.getName());
                    hdfsFileSystem.copyFromLocalFile(false, true, localJarPath, hdfsJarPath);
                }
            }
        }

    }

    /**
     * Returns a set of all files in the given path.
     *
     * @param localPath the local path.
     * @return a set of files.
     * @throws IllegalArgumentException if {@code localPath} does not exist.
     */
    private Set<File> collectFilesFromLocalDir(String localPath) throws IllegalArgumentException {
        File path = new File(localPath);

        if (!path.isDirectory()) {
            throw new IllegalArgumentException(String.format("Path points to file, not directory: %s", localPath));
        }

        Set<File> files = new HashSet<File>();
        for (File file : path.listFiles()) {
            if (file.exists() && !file.isDirectory()) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Logs the command alternative for executing the job from the command line.
     *
     * @param jar the MapReduce job jar.
     * @param main the main class to initialize.
     * @param arguments the arguments to use.
     */
    private void logCommand(String jar, String main, List<String> arguments) {
        StringBuilder text = new StringBuilder();

        text.append(String.format("bin/hadoop jar %s \\", jar));
        text.append(System.lineSeparator());
        text.append(String.format("%s \\", main));
        text.append(System.lineSeparator());
        if (!arguments.isEmpty()) {
            for (int index = 0, count = arguments.size() - 1; index < count; index++) {
                text.append(String.format("\"%s\" \\", arguments.get(index)));
                text.append(System.lineSeparator());
            }
            text.append(String.format("%s", arguments.get(arguments.size() - 1)));
        }

        logger.info(text.toString());
    }
}
