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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.IOUtils;

public class RunJar {

	public static final Pattern MATCH_ANY = Pattern.compile(".*");

	public static void unJar(File jarFile, File toDir) throws IOException {
		unJar(jarFile, toDir, MATCH_ANY);
	}

	public static void unJar(File jarFile, File toDir, Pattern unpackRegex) throws IOException {
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

	private static void ensureDirectory(File dir) throws IOException {
		if (!dir.mkdirs() && !dir.isDirectory()) {
			throw new IOException("Mkdirs failed to create " + dir.toString());
		}
	}

	public void run(Map<String, String> properties) throws Throwable {
		if (!properties.containsKey("mapreduce.job.name")) {
			throw new Exception("Job name is not set.");
		}
		if (!properties.containsKey("mapreduce.job.jar")) {
			throw new Exception("JAR is not set.");
		}

		String fileName = properties.get("mapreduce.job.jar");
		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			throw new Exception(String.format("Not a valid JAR: %s", file.getCanonicalPath()));
		}

		String mainClassName = null;

		JarFile jarFile;
		try {
			jarFile = new JarFile(fileName);
		} catch (IOException io) {
			throw new IOException("Error opening job jar: " + fileName).initCause(io);
		}

		Manifest manifest = jarFile.getManifest();
		if (manifest != null) {
			mainClassName = manifest.getMainAttributes().getValue("Main-Class");
		}
		jarFile.close();

		if (mainClassName == null) {
			if (!properties.containsKey("mapreduce.job.main")) {
				throw new Exception("Main Class is not set.");
			}
			mainClassName = properties.get("mapreduce.job.main");
		}
		mainClassName = mainClassName.replaceAll("/", ".");

		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		ensureDirectory(tmpDir);

		final File workDir;
		try {
			workDir = File.createTempFile("hadoop-unjar", "", tmpDir);
		} catch (IOException ioe) {
			throw new Exception("Error creating temp dir in java.io.tmpdir " + tmpDir + " due to " + ioe.getMessage());
		}

		if (!workDir.delete()) {
			throw new Exception("Delete failed for " + workDir);
		}
		ensureDirectory(workDir);

		unJar(file, workDir);

		ClassLoader loader = createClassLoader(file, workDir);

		Thread.currentThread().setContextClassLoader(loader);
		Class<?> mainClass = Class.forName(mainClassName, true, loader);

		// Construct invocation arguments
		List<String> arguments = new ArrayList<>();
		for (String key : properties.keySet()) {
			switch (key) {
				case "mapreduce.job.jar":
					// Ignore JAR file name
					break;
				case "mapreduce.job.main":
					// Ignore Main Class name
					break;
				default:
					arguments.add(key + "=" + properties.get(key));
					break;
			}
		}

		Method main = mainClass.getMethod("main", new Class[] { Array.newInstance(String.class, 0).getClass() });

		try {
			main.invoke(null, new Object[] { arguments.toArray(new String[0]) });
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private ClassLoader createClassLoader(File file, final File workDir) throws MalformedURLException, IOException {
		List<URL> classPath = new ArrayList<URL>();
		classPath.add(new File(workDir + "/").toURI().toURL());
		classPath.add(file.toURI().toURL());
		classPath.add(new File(workDir, "classes/").toURI().toURL());

		transferJarsToWorlingDir(new File(workDir, "lib"));

		File[] libs = new File(workDir, "lib").listFiles();
		if (libs != null) {
			for (int i = 0; i < libs.length; i++) {
				classPath.add(libs[i].toURI().toURL());
			}
		}

		return new URLClassLoader(classPath.toArray(new URL[0]));
	}

	boolean useClientClassLoader() {
		return false;
	}

	private void transferJarsToWorlingDir(File target) throws IOException {
		try {
			File source = new File("/home/daiad/Development/hbase-mapreduce/lib/");

			FileUtils.copyDirectory(source, target);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
