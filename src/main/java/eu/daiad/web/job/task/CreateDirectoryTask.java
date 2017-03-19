package eu.daiad.web.job.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.service.scheduling.Constants;

/**
 * Task for creating a directory on the local file system or HDFS.
 */
@Component
public class CreateDirectoryTask extends BaseTask implements StoppableTasklet {

    /**
     * Environment specific temporary directory.
     */
    private static final String PROPERTY_TMP_DIRECTORY = "java.io.tmpdir";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            String basePath = parameters.get(EnumInParameter.BASE_PATH.getValue());
            if (StringUtils.isBlank(basePath)) {
                basePath = System.getProperty(PROPERTY_TMP_DIRECTORY);
            }

            String prefix = parameters.get(EnumInParameter.PREFIX.getValue());
            if (StringUtils.isBlank(prefix)) {
                prefix = chunkContext.getStepContext().getJobName() + "-";
            }

            String newDirectory = null;
            if (StringUtils.isBlank(parameters.get(EnumInParameter.HDFS_PATH.getValue()))) {
                newDirectory = createLocalDirectory(basePath, prefix);
            } else {
                newDirectory = createHdfsDirectory(parameters.get(EnumInParameter.HDFS_PATH.getValue()), basePath, prefix);
            }

            String key = chunkContext.getStepContext().getStepName() +
                         Constants.PARAMETER_NAME_DELIMITER +
                         EnumOutParameter.NEW_DIRECTORY.getValue();

            chunkContext.getStepContext()
                        .getStepExecution()
                        .getExecutionContext()
                        .put(key, newDirectory);

            if (!StringUtils.isBlank(parameters.get(EnumInParameter.HDFS_PATH.getValue()))) {
                key = chunkContext.getStepContext().getStepName() +
                      Constants.PARAMETER_NAME_DELIMITER +
                      EnumOutParameter.HDFS_PATH.getValue();

                chunkContext.getStepContext()
                            .getStepExecution()
                            .getExecutionContext()
                            .put(key, parameters.get(EnumInParameter.HDFS_PATH.getValue()));
            }
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED)
                .set("step", chunkContext.getStepContext().getStepName());
        }

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Creates a directory on the local file system.
     *
     * @param basePath the base path where the new directory is created.
     * @param prefix prefix for the new directory name.
     * @return a valid directory path.
     * @throws Exception if an I/O exception occurs.
     */
    private String createLocalDirectory(String basePath, String prefix) throws Exception {
        File tmpDir = new File(basePath);
        ensureDirectory(tmpDir);

        final File directory;
        try {
            directory = File.createTempFile(prefix, "", tmpDir);
        } catch (IOException ioe) {
            throw new Exception(String.format("Error creating directory in java.io.tmpdir %s due to %s.", tmpDir, ioe.getMessage()));
        }

        if (!directory.delete()) {
            throw new Exception("Delete failed for " + directory);
        }
        ensureDirectory(directory);

        return directory.getPath();
    }

    // TODO: Move fs.defaultFS to a constant.

    /**
     * Creates a directory on the HDFS file system.
     *
     * @param filesystem HDFS file system.
     * @param basePath the base path where the new directory is created.
     * @param prefix prefix for the new directory name.
     * @return a valid directory path.
     * @throws Exception if an I/O exception occurs.
     */
    private String createHdfsDirectory(String filesystem, String basePath, String prefix) throws Exception {
        final String directory;

        directory = Paths.get(basePath, prefix + RandomStringUtils.randomAlphanumeric(8)).toString();

        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", filesystem);

        FileSystem fs = FileSystem.get(conf);
        fs.mkdirs(new Path(directory));

        return filesystem + directory + "/";
    }

    /**
     * Enumeration of task input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Base path where the new directory is created. Default value is the system's temporary directory.
         */
        BASE_PATH("base.path"),
        /**
         * Prefix for the new directory. Default value is the job name.
         */
        PREFIX("prefix"),
        /**
         * HDFS path. If set, the directory is created on HDFS.
         */
        HDFS_PATH("fs.defaultFS");


        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

    /**
     * Enumeration of task output parameters.
     */
    public static enum EnumOutParameter {
        /**
         * The new directory
         */
        NEW_DIRECTORY("new.directory"),
        /**
         * HDFS path.
         */
        HDFS_PATH("fs.defaultFS");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumOutParameter(String value) {
            this.value = value;
        }
    }
}
