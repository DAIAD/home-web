package eu.daiad.web.job.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.connector.RemoteFileAttributes;
import eu.daiad.web.connector.SecureFileTransferConnector;
import eu.daiad.web.connector.SftpProperties;
import eu.daiad.web.domain.admin.UploadEntity;

/**
 * Task for submitting an Apache Flink job to a YARN cluster.
 */
@Component
public class SftpTransferTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(SftpTransferTask.class);

    /**
     * Secure FTP connection.
     */
    @Autowired
    SecureFileTransferConnector sftConnector;

    /**
     * Entity manager for persisting upload meta data.
     */
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Extracts SFTP parameters from task parameters.
     *
     * @param parameters the task parameters.
     * @return the SFTP properties.
     */
    private SftpProperties getSftpProperties(Map<String, String> parameters) {
        String host = parameters.get(EnumInParameter.SFTP_HOST.getValue());
        String port = parameters.get(EnumInParameter.SFTP_PORT.getValue());
        String username = parameters.get(EnumInParameter.SFTP_USERNAME.getValue());
        String password = parameters.get(EnumInParameter.SFTP_PASSWORD.getValue());

        return new SftpProperties(host, Integer.parseInt(port), username, password);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            // Get parameters
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            SftpProperties sftpProperties = getSftpProperties(parameters);

            String remotePath = parameters.get(EnumInParameter.REMOTE_FOLDER.getValue());
            String localPath = parameters.get(EnumInParameter.WORKING_DIRECTORY.getValue());

            String regex = parameters.get(EnumInParameter.FILTER_REGEX.getValue());
            Pattern allowedFilenames = null;
            if (!StringUtils.isBlank(regex)) {
                allowedFilenames = Pattern.compile(regex);
            }

            // Enumerate files from the remote folder
            ArrayList<RemoteFileAttributes> files = sftConnector.ls(sftpProperties, remotePath);

            for (RemoteFileAttributes f : files) {
                if ((allowedFilenames != null) && (!allowedFilenames.matcher(f.getFilename()).matches())) {
                    continue;
                }

                // Skip duplicate files
                String sqlString = "select      u " +
                                   "from        upload u " +
                                   "where       u.remoteFolder = :remoteFolder and " +
                                   "            u.remoteFilename = :remoteFilename " +
                                   "order by    u.id desc";

                TypedQuery<UploadEntity> uploadQuery = entityManager.createQuery(sqlString, UploadEntity.class).setFirstResult(0).setMaxResults(1);

                uploadQuery.setParameter("remoteFolder", f.getRemoteFolder());
                uploadQuery.setParameter("remoteFilename", f.getFilename());

                List<UploadEntity> uploads = uploadQuery.getResultList();
                if (!uploads.isEmpty()) {
                    continue;
                }

                // Create upload record
                UploadEntity upload = new UploadEntity();

                upload.setSource(f.getSource());
                upload.setRemoteFolder(f.getRemoteFolder());
                upload.setRemoteFilename(f.getFilename());

                upload.setSize(f.getSize());
                upload.setModifiedOn(f.getModifiedOn());

                upload.setLocalFolder(localPath);
                upload.setLocalFilename(UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(f.getFilename()));

                String target = FilenameUtils.concat(upload.getLocalFolder(), upload.getLocalFilename());

                // Download file to the local folder
                upload.setUploadStartedOn(new DateTime());
                sftConnector.get(sftpProperties, remotePath, f.getFilename(), target);
                upload.setUploadCompletedOn(new DateTime());

                entityManager.persist(upload);
                entityManager.flush();
            }
        } catch (Throwable t) {
            logger.fatal("Failed to transfer files from SFTP server.", t);

            throw t;
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
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
         * SFTP server host address.
         */
        SFTP_HOST("sftp.host"),
        /**
         * SFPT server port.
         */
        SFTP_PORT("sftp.port"),
        /**
         * SFTP user name.
         */
        SFTP_USERNAME("sftp.username"),
        /**
         * SFTP user password.
         */
        SFTP_PASSWORD("sftp.password"),
        /**
         * Remote folder.
         */
        REMOTE_FOLDER("folder.remote"),
        /**
         * Regular expression for filtering remote file names.
         */
        FILTER_REGEX("filter.regex");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

}
