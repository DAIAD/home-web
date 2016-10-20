package eu.daiad.web.service.etl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Helper abstract class that provides utility methods to services that export
 * utility data.
 */
public abstract class AbstractUtilityDataExportService extends AbstractDataExportService {

    /**
     * Creates a filename for the utility exported data file.
     * 
     * @param targetDirectory target directory 
     * @param filenamePrefix filename prefix.
     * @param extension filename extension.
     * @return the filename.
     */
    protected String createUtilityExportFilename(String targetDirectory, String filenamePrefix, String extension) {
        String filename;
        
        if(StringUtils.isBlank(filenamePrefix)) {
            filenamePrefix = "export";
        } else {
            filenamePrefix = filenamePrefix.replaceAll("[^a-zA-Z0-9]", "-");
        }
        
        DateTimeFormatter fileDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        if(StringUtils.isBlank(extension)) {
            filename = String.format("%s-%s", filenamePrefix.toLowerCase(), new DateTime().toString(fileDateFormatter));    
        } else {
            filename = String.format("%s-%s.%s", filenamePrefix.toLowerCase(), new DateTime().toString(fileDateFormatter), extension);
        }
        

        return FilenameUtils.concat(targetDirectory, filename);
    }
    
}
