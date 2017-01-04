package eu.daiad.web.job.task;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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

/**
 * Deletes a directory on the local file system or HDFS.
 */
@Component
public class DeleteDirectoryTask extends BaseTask implements StoppableTasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            if (StringUtils.isBlank(parameters.get(EnumInParameter.HDFS_PATH.getValue()))) {
                deleteLocalDirectory(parameters.get(EnumInParameter.INPUT_DIRECTORY.getValue()));
            } else {
                deleteHdfsDirectory(parameters.get(EnumInParameter.INPUT_DIRECTORY.getValue()),
                                    parameters.get(EnumInParameter.HDFS_PATH.getValue()));
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
     * Deletes a directory from the local file system.
     *
     * @param path the directory to delete.
     * @throws Exception if an I/O exception occurs.
     */
    private void deleteLocalDirectory(String path) throws Exception {
        File directory = new File(path);

        try {
            if(directory.exists()) {
                FileUtils.deleteQuietly(directory);
            }
        } catch (Exception ex) {
            throw new Exception(String.format("Error deleting directory %s due to %s.", path, ex.getMessage()));
        }
    }

    // TODO: Move fs.defaultFS to a constant.

    /**
     * Creates a temporary working directory on the local file system.
     *
     * @param path the directory to delete
     * @param filesystem HDFS file system.
     * @throws Exception if an I/O exception occurs.
     */
    private void deleteHdfsDirectory(String path, String filesystem) throws Exception {
        Path hdfsPath = new Path(path);

        try {
            Configuration conf = new Configuration();
            conf.set("fs.defaultFS", filesystem);

            FileSystem fs = FileSystem.get(conf);
            if (fs.exists(hdfsPath)) {
                fs.delete(hdfsPath, true);
            }
        } catch (Exception ex) {
            throw new Exception(String.format("Error deleting directory %s due to %s.", path, ex.getMessage()));
        }
    }

    /**
     * Enumeration of task input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Directory that must be deleted
         */
        INPUT_DIRECTORY("input.directory"),
        /**
         * HDFS path.
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

}
