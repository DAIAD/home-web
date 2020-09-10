package eu.daiad.web.job.task;

import java.io.File;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.admin.UploadEntity;
import eu.daiad.web.model.loader.EnumUploadFileType;
import eu.daiad.web.model.loader.FileProcessingStatus;
import eu.daiad.web.service.IWaterMeterDataLoaderService;

/**
 * Task for exporting smart water meter data for a utility.
 */
@Component
public class ImportMeterDataTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ImportMeterDataTask.class);

    /**
     * Entity manager for persisting upload meta data.
     */
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Service for downloading, parsing and importing smart water meter data to HBase.
     */
    @Autowired
    private IWaterMeterDataLoaderService waterMeterDataLoaderService;

    /**
     * Returns a set of all files in the given path.
     *
     * @param localPath the local path.
     * @return a set of files.
     * @throws IllegalArgumentException if {@code localPath} does not exist.
     */
    private List<File> collectFilesFromLocalDir(String localPath) throws IllegalArgumentException {
        File path = new File(localPath);

        if (!path.exists()) {
            throw new IllegalArgumentException(String.format("Path %s does not exist.", localPath));
        }

        if (!path.isDirectory()) {
            throw new IllegalArgumentException(String.format("Path points to file, not directory: %s", localPath));
        }

        List<File> files = new ArrayList<File>();
        for (File file : path.listFiles()) {
            if (file.exists() && !file.isDirectory()) {
                files.add(file);
            }
        }
        return files;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception{
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Check source/target directories
            String sourceDir = parameters.get(EnumInParameter.WORKING_DIRECTORY.getValue());
            String targetDir = parameters.get(EnumInParameter.STORAGE_DIRECTORY.getValue());

            if (StringUtils.isBlank(sourceDir)) {
                throw new ExportException("Source directory is not set.");
            }
            if (StringUtils.isBlank(targetDir)) {
                throw new ExportException("Target directory is not set.");
            }
            if (sourceDir.equals(targetDir)) {
                throw new ExportException("Source and target directories cannot be the same.");
            }

            // Create target folder
            FileUtils.forceMkdir(new File(targetDir));

            // Set time zone
            String timezone = parameters.get(EnumInParameter.TIMEZONE.getValue());
            if (StringUtils.isBlank(timezone)) {
                throw new ExportException("Time zone is not set.");
            }
            Set<String> zones = DateTimeZone.getAvailableIDs();
            if (!zones.contains(timezone)) {
                throw new ExportException(String.format("Time zone [%s] is not supported.", timezone));
            }

            // Import every file in the working directory and store it permanently.
            List<File> files = collectFilesFromLocalDir(sourceDir);
            Collections.sort(files, new Comparator<File>() {

                @Override
                public int compare(File f1, File f2) {
                    long result = f1.lastModified() - f2.lastModified();

                    if (result < 0 ) {
                        return -1;
                    } else if (result > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            for (File tempFile : files) {
                // Check if a file with the same path and name has already been imported
                String sqlString = "select      u " +
                                   "from        upload u " +
                                   "where       u.localFolder = :localFolder and " +
                                   "            u.localFilename = :localFilename " +
                                   "order by    u.id desc";

                TypedQuery<UploadEntity> uploadQuery = entityManager.createQuery(sqlString, UploadEntity.class).setFirstResult(0).setMaxResults(1);

                uploadQuery.setParameter("localFolder", tempFile.getParent());
                uploadQuery.setParameter("localFilename", tempFile.getName());

                List<UploadEntity> uploads = uploadQuery.getResultList();

                UploadEntity existingUpload = null;
                if (uploads.size() != 0) {
                    existingUpload = uploads.get(0);
                }

                if ((existingUpload == null) ||
                    (existingUpload.getProccessedRows() == 0) ||
                    ((existingUpload.getSkippedRows() + existingUpload.getProccessedRows()) != existingUpload.getTotalRows())) {

                    // Create record if not one already exists
                    UploadEntity upload = null;
                    if (existingUpload == null) {
                        upload =  new UploadEntity();
                    } else {
                        upload = existingUpload;
                    }

                    // Copy filename
                    upload.setLocalFolder(targetDir);
                    upload.setLocalFilename(UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(tempFile.getName()));

                    String target = FilenameUtils.concat(upload.getLocalFolder(), upload.getLocalFilename());
                    File targetFile = new File(target);

                    FileUtils.copyFile(tempFile, targetFile);

                    if (existingUpload == null) {
                        upload.setModifiedOn(new DateTime(targetFile.lastModified()));
                        upload.setSize(targetFile.length());
                    }


                    // Process data and import records to HBASE
                    upload.setProcessingStartedOn(new DateTime());

                    FileProcessingStatus status = waterMeterDataLoaderService.parse(target, timezone, EnumUploadFileType.METER_DATA, null);

                    upload.setTotalRows(status.getTotalRows());
                    upload.setProccessedRows(status.getProcessedRows());
                    upload.setSkippedRows(status.getSkippedRows());
                    upload.setNegativeDifferenceRows(status.getNegativeDifference());
                    upload.setProcessingCompletedOn(new DateTime());
                    if (!StringUtils.isBlank(status.getFilename())) {
                        upload.setLocalFilename(status.getFilename());
                    }

                    // Save record if this is a new file (not an uploaded file)
                    if (existingUpload == null) {
                        entityManager.persist(upload);
                    }
                    entityManager.flush();
                }
            }
        } catch (Exception ex) {
            logger.fatal("Failed to import meter data to HBASE.", ex);

            throw ex;
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {

    }

    /**
     * Enumeration of task input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Working directory path
         */
        WORKING_DIRECTORY("working.dir"),
        /**
         * Local folder for storing imported files
         */
        STORAGE_DIRECTORY("folder.local"),
        /**
         * Data time zone
         */
        TIMEZONE("timezone");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

}
