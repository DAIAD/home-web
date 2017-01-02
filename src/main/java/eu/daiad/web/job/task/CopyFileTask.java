package eu.daiad.web.job.task;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
 * Task for exporting smart water meter data for a utility.
 */
@Component
public class CopyFileTask extends BaseTask implements StoppableTasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            // Get all step parameters
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            String source = parameters.get(EnumInParameter.SOURCE_PATH.getValue());
            String target = parameters.get(EnumInParameter.TARGET_PATH.getValue());

            if (StringUtils.isBlank(parameters.get(EnumInParameter.HDFS_PATH.getValue()))) {
                copyFilesToLocalFilesystem(parameters, source, target);
            } else {
                copyFilesToHdfs(parameters, source, target);
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

    // TODO: Move fs.defaultFS to a constant.

    /**
     * Copies files from the {@code source} folder to the {@code target} folder on HDFS.
     *
     * @param parameters the job parameters.
     * @param source the source directory.
     * @param target the target directory.
     * @throws IOException if an I/O exception occurs.
     */
    private void copyFilesToHdfs(Map<String, String> parameters, String source, String target) throws IOException {
        if (!StringUtils.isBlank(source)) {
            Set<File> files = collectFilesFromLocalDir(source);

            if (!files.isEmpty()) {
                System.err.println(String.format("Copying [%d] files from local dir [%s] to HDFS dir [%s] at [%s]",
                                                 files.size(),
                                                 source,
                                                 target,
                                                 parameters.get(EnumInParameter.HDFS_PATH.getValue())));

                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", parameters.get(EnumInParameter.HDFS_PATH.getValue()));

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
     * Copies files from the {@code source} folder to the {@code target} folder.
     *
     * @param parameters the job parameters.
     * @param source the source directory.
     * @param target the target directory.
     * @throws IOException if an I/O exception occurs.
     */
    private void copyFilesToLocalFilesystem(Map<String, String> parameters, String source, String target) throws IOException {
        if (!StringUtils.isBlank(source)) {
            Set<File> files = collectFilesFromLocalDir(source);

            if (!files.isEmpty()) {
                System.err.println(String.format("Copying [%d] files from local dir [%s] to dir [%s]",
                                                 files.size(),
                                                 source,
                                                 target));

                for (File file : files) {
                    FileUtils.copyFile(file, new File(FilenameUtils.concat(target,file.getName())));
                }
            }
        }

    }

    /**
     * Enumeration of job input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Source directory.
         */
        SOURCE_PATH("path.source"),
        /**
         * Target directory.
         */
        TARGET_PATH("path.target"),
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
