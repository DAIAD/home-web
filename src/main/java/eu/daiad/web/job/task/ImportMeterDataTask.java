package eu.daiad.web.job.task;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.connector.SftpProperties;
import eu.daiad.web.job.builder.WaterMeterDataSecureFileTransferJobBuilder;
import eu.daiad.web.model.loader.DataTransferConfiguration;
import eu.daiad.web.service.IWaterMeterDataLoaderService;

/**
 * Task for exporting smart water meter data for a utility.
 */
@Component
public class ImportMeterDataTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(WaterMeterDataSecureFileTransferJobBuilder.class);

    /**
     * Service for downloading, parsing and importing smart water meter data to HBase.
     */
    @Autowired
    private IWaterMeterDataLoaderService loader;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Initialize configuration
            DataTransferConfiguration config = new DataTransferConfiguration();

            // File properties
            config.setLocalFolder(parameters.get(EnumParameter.FOLDER_LOCAL.getValue()));
            config.setRemoteFolder(parameters.get(EnumParameter.FOLDER_REMOTE.getValue()));
            config.setTimezone(parameters.get(EnumParameter.TIMEZONE.getValue()));

            // Filter properties
            config.setFilterRegEx(parameters.get(EnumParameter.FILENAME_REGEX_FILTER.getValue()));

            // SFTP properties
            String host = parameters.get(EnumParameter.SFTP_HOST.getValue());
            int port = Integer.parseInt(parameters.get(EnumParameter.SFTP_PORT.getValue()));
            String username = parameters.get(EnumParameter.SFTP_USERNAME.getValue());
            String password = parameters.get(EnumParameter.SFTP_PASSWORD.getValue());

            config.setSftpProperties(new SftpProperties(host, port, username, password));

            loader.load(config);
        } catch (Exception ex) {
            logger.fatal("Failed to load meter data from SFTP server.", ex);

            throw ex;
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {

    }

    /**
     * Enumeration of job parameters.
     */
    public static enum EnumParameter {
        /**
         * Empty parameter
         */
        EMPTY(null),
        /**
         * SFTP server name
         */
        SFTP_HOST("sftp.host"),
        /**
         * SFTP server port
         */
        SFTP_PORT("sftp.port"),
        /**
         * SFTP server user name
         */
        SFTP_USERNAME("sftp.username"),
        /**
         * SFTP server password
         */
        SFTP_PASSWORD("sftp.password"),
        /**
         * Local folder for storing files
         */
        FOLDER_LOCAL("folder.local"),
        /**
         * SFTP remote folder
         */
        FOLDER_REMOTE("folder.remote"),
        /**
         * Data time zone
         */
        TIMEZONE("timezone"),
        /**
         * Regular expression for filtering file names
         */
        FILENAME_REGEX_FILTER("filter.regex");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumParameter(String value) {
            this.value = value;
        }

        public static EnumParameter fromString(String value) {
            for (EnumParameter item : EnumParameter.values()) {
                if (item.name().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return EMPTY;
        }
    }
}
