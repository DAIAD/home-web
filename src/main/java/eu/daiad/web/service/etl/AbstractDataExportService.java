package eu.daiad.web.service.etl;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.service.BaseService;

/**
 * Helper abstract class that provides utility methods to data export services
 * that derive from it.
 */
public abstract class AbstractDataExportService extends BaseService {

    /**
     * Delimiter character used for separating values in export text output files. 
     */
    protected static final char DELIMITER = ';';
    
    /**
     * Folder where temporary files are saved. 
     */
    @Value("${tmp.folder}")
    protected String workingDirectory;

    /**
     * Initializes the working directory.
     * 
     * @throws ApplicationException
     *             in case an I/O exception has occurred.
     */
    protected void ensureWorkingDirectory() throws ApplicationException {
        File outputFolder = new File(this.workingDirectory);

        outputFolder.mkdirs();

        if (!outputFolder.exists()) {
            throw createApplicationException(SharedErrorCode.DIR_CREATION_FAILED).set("path", this.workingDirectory);
        }
    }

    /**
     * Initializes the working directory.
     * 
     * @param path directory to check
     * @throws ApplicationException in case an I/O exception has occurred.
     */
    protected void ensureDirectory(String path) throws ApplicationException {
        File directory = new File(path);

        directory.mkdirs();

        if (!directory.exists()) {
            throw createApplicationException(SharedErrorCode.DIR_CREATION_FAILED).set("path", path);
        }        
    }

    /**
     * Validates the given time zone.
     * 
     * @param timezone the time zone to validate.
     * @throws ApplicationException if time zone is empty or invalid.
     */
    protected void ensureTimezone(String timezone) throws ApplicationException {
        if (StringUtils.isBlank(timezone)) {
            throw createApplicationException(SharedErrorCode.INVALID_TIME_ZONE).set("timezone", timezone);
        }
        
        Set<String> zones = DateTimeZone.getAvailableIDs();

        if (!zones.contains(timezone)) {
            throw createApplicationException(SharedErrorCode.TIMEZONE_NOT_FOUND).set("timezone", timezone);
        }
    }

    /**
     * Creates a temporary file with a random name in the working directory.
     * 
     * @param workingDirectory the directory where intermediate files are stored.
     */
    protected String createTemporaryFilename(String workingDirectory) {
        String filename = UUID.randomUUID().toString();
        
        return FilenameUtils.concat(workingDirectory, filename);
    }
}
