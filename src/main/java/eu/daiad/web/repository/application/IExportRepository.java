package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.domain.admin.ExportFileEntity;
import eu.daiad.web.model.export.ExportFile;

/**
 * Provides methods for managing exported data files
 */
public interface IExportRepository {

    /**
     * Store an instance of {@link ExportFileEntity}.
     * 
     * @param entity the entity to store.
     */
    public void create(ExportFileEntity entity);

    /**
     * Get an exported file with the given id.
     * 
     * @param id of the file.
     * @return the exported file.
     */
    ExportFile getExportFileById(int id);
    
    /**
     * Get an exported file with the given key.
     * 
     * @param key of the file.
     * @return the exported file.
     */
    ExportFile getExportFileByKey(UUID key);
    
    /**
     * Get all exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @return a list of the exported data files.
     */
    List<ExportFile> getAllExportFilesByUtilityId(int utilityId);
    
    /**
     * Get all valid exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @param days the number of days after which a file is marked as expired.
     * @return a list of valid exported data files.
     */
    List<ExportFile> getValidExportFilesByUtilityId(int utilityId, int days);

    /**
     * Get all expired exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @param days the number of days after which a file is marked as expired.
     * @return a list of expired exported data files.
     */
    List<ExportFile> getExpiredExportFilesByUtilityId(int utilityId, int days);
    
    /**
     * Deletes all expired exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @param days the number of days after which a file is marked as expired.
     */
    void deleteExpiredExportFiles(int utilityId, int days);

}
