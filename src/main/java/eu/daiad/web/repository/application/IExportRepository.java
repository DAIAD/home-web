package eu.daiad.web.repository.application;

import java.util.UUID;

import eu.daiad.web.domain.admin.ExportFileEntity;
import eu.daiad.web.model.export.DataExportFileQuery;
import eu.daiad.web.model.export.DataExportFileQueryResult;
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
     * @param query query that selects exported data files.
     * @return a list of the exported data files.
     */
    DataExportFileQueryResult getAllExportFiles(DataExportFileQuery query);
    
    /**
     * Get all valid exported files for a specific utility.
     * 
     * @param query query that selects exported data files.
     * @return a list of valid exported data files.
     */
    DataExportFileQueryResult getValidExportFiles(DataExportFileQuery query);

    /**
     * Get all expired exported files for a specific utility.
     * 
     * @param query query that selects exported data files.
     * @return a list of expired exported data files.
     */
    DataExportFileQueryResult getExpiredExportFiles(DataExportFileQuery query);
    
    /**
     * Deletes all expired exported files for a specific utility.
     * 
     * @param utilityId the utility id.
     * @param days the number of days after which a file is marked as expired.
     */
    void deleteExpiredExportFiles(int utilityId, int days);

}
