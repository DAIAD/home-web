package eu.daiad.web.service.etl;

import eu.daiad.web.model.error.ApplicationException;

/**
 * Provides methods for exporting data from HBASE and PostgreSQL
 */
public interface IDataExportService {

    /**
     * Exports data for a single user to a temporary file.
     *
     * @param query the query that selects the data to export.
     * @return a unique token for downloading the exported file.
     *
     * @throws ApplicationException if the query execution or file creation fails.
     */
    String export(UserDataExportQuery query) throws ApplicationException;

    /**
     * Exports data for a single utility to a file. Any exported data file is replaced.
     *
     * @param query the query that selects the data to export.
     *
     * @throws ApplicationException if the query execution or file creation fails.
     */
    void export(UtilityDataExportQuery query) throws ApplicationException;

}
