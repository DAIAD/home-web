package eu.daiad.common.repository.application;

import java.util.UUID;

import eu.daiad.common.domain.admin.ExportFileEntity;
import eu.daiad.common.model.export.DataExportFileQuery;
import eu.daiad.common.model.export.DataExportFileQueryResult;
import eu.daiad.common.model.export.ExportFile;

/**
 * Provides methods for managing exported data files
 */
public interface IExportRepository {

    /**
     * Store an instance of {@link ExportFileEntity}.
     *
     * @param entity the entity to store.
     */
    void create(ExportFileEntity entity);

    /**
     * Store an instance of {@link ExportFileEntity}, replacing any export with the same filename.
     *
     * @param entity the entity to store.
     */
    void replace(ExportFileEntity entity);

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
    DataExportFileQueryResult getNotExpiredExportFiles(DataExportFileQuery query);

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
