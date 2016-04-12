package eu.daiad.web.service;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.export.ExportUserDataQuery;

public interface IExportService {

	public abstract String export(ExportUserDataQuery data) throws ApplicationException;

}