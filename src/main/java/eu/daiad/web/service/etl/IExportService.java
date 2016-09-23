package eu.daiad.web.service.etl;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.export.ExportUserDataQuery;

public interface IExportService {

	/**
	 * Exports all Amphiro B1 sessions and smart water meter readings for a single user to a temporary file.
	 *  
	 * @param data a query that selects the data to export.
	 * @return a unique token for downloading the exported file.
	 * @throws ApplicationException if the query execution or file creation fails.
	 */
	public abstract String export(ExportUserDataQuery data) throws ApplicationException;

}