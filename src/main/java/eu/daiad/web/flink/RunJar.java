package eu.daiad.web.flink;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.batch.core.ExitStatus;

import eu.daiad.web.job.builder.CommandExecutor;

public class RunJar {

    /**
     * Environment specific temporary directory.
     */
    private static final String PROPERTY_TMP_DIRECTORY = "java.io.tmpdir";

    /**
     * Environment specific new line separator.
     */
    private static final String PROPERTY_NEW_LINE = "line.separator";

    /**
     * Default execution timeout set to 1 hour.
     */
    private static final long DEFAULT_TIMEOUT = 60 * 60 * 1000;

    /**
     * Executes a map reduce job from an external jar file.
     *
     * @param properties properties to add to the job configuration.
     * @return the exit status of the job execution.
     * @throws Throwable if the job execution fails.
     */
    public int run(Map<String, String> properties) throws Throwable {
        ensureParameter(properties, EnumFlinkParameter.JOB_NAME.getValue(), "Job name is not set");
        ensureParameter(properties, EnumFlinkParameter.JOB_SCRIPT.getValue(), "Job script is not set");

        ensureFile(properties.get(EnumFlinkParameter.JOB_SCRIPT.getValue()));
        ensureParameter(properties, EnumFlinkParameter.WORKING_DIRECTORY.getValue(), "Flink working directory on HDFS is not set");

        // Create working directory
        File tmpDir = new File(System.getProperty(PROPERTY_TMP_DIRECTORY));
        ensureDirectory(tmpDir);

        final File workDir;
        try {
            workDir = File.createTempFile("yarn-flink-", "", tmpDir);
        } catch (IOException ioe) {
            throw new Exception(String.format("Error creating temp dir in java.io.tmpdir %s due to %s.", tmpDir, ioe.getMessage()));
        }

        if (!workDir.delete()) {
            throw new Exception("Delete failed for " + workDir);
        }
        ensureDirectory(workDir);

        try {
            // Construct invocation arguments
            String command = getCommand(properties);

            CommandExecutor commandExecutor = new CommandExecutor(command,
                                                                  DEFAULT_TIMEOUT,
                                                                  workDir.getAbsolutePath());

            if (commandExecutor.execute() == ExitStatus.COMPLETED) {
                String[] lines = commandExecutor.getOutput();

                File logFile = File.createTempFile(properties.get(EnumFlinkParameter.JOB_NAME.getValue()) + "-", "", tmpDir);
                FileUtils.writeStringToFile(logFile, StringUtils.join(lines, System.getProperty(PROPERTY_NEW_LINE)));

                return 0;
            } else {
                String[] lines = commandExecutor.getOutput();

                File logFile = File.createTempFile(properties.get(EnumFlinkParameter.JOB_NAME.getValue()) + "-", "", tmpDir);
                FileUtils.writeStringToFile(logFile, StringUtils.join(lines, System.getProperty(PROPERTY_NEW_LINE)));

                return -1;
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            // Delete temporary files
            FileUtils.deleteQuietly(workDir);
        }
    }

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
     * Creates a directory.
     *
     * @param dir the directory to create.
     * @throws IOException if an I/O exception occurs.
     */
    private void ensureDirectory(File dir) throws IOException {
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException(String.format("Mkdirs failed to create %s", dir.toString()));
        }
    }

    /**
     * Checks if a file exists.
     *
     * @param filename the file to check.
     * @throws IOException if an I/O exception occurs.
     */
    private void ensureFile(String filename) throws IOException {
        File file = new File(filename);
        if ((!file.exists()) || (file.isDirectory())) {
            throw new IOException(String.format("File %s does not exists or it is a directory", filename));
        }
    }

    /**
     * Replaces all command line arguments with job parameters. Arguments names
     * are enclosed in curly brackets e.g. {parameter}.
     *
     * @param command the command.
     * @param properties the job parameters.
     * @return the command with all arguments set.
     */
    private String getCommand(Map<String, String> properties) {
        String command = properties.get(EnumFlinkParameter.JOB_SCRIPT.getValue());
        String arguments = properties.get(EnumFlinkParameter.JOB_SCRIPT_ARGS.getValue());

        if (!StringUtils.isBlank(arguments)) {
            command = command + " " + arguments;
            // Get all parameter names
            List<String> matches = new ArrayList<String>();
            Matcher matcher = Pattern.compile("(\\{[^\\{]*\\})").matcher(command);
            while (matcher.find()) {
                matches.add(matcher.group());
            }
            // Replace every parameter placeholder with job parameter value
            for (String match : matches) {
                if (match.length() == 2) {
                    throw new IllegalArgumentException(String.format("Empty parameter found in command [%s].", command));
                }
                String key = match.substring(1, match.length() - 1);
                if (StringUtils.isBlank(key)) {
                    throw new IllegalArgumentException(String.format("Invalid parameter name found in command [%s].", command));
                }
                if (!properties.containsKey(key)) {
                    throw new IllegalArgumentException(String.format("Parameter [%s] does not exist.", key));
                }
                command = command.replace(match, properties.get(key));
            }
        }
        return command;
    }

}
