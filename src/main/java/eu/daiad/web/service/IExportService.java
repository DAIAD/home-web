package eu.daiad.web.service;

import eu.daiad.web.model.ExportException;
import eu.daiad.web.model.export.ExportDataRequest;

public interface IExportService {

	public abstract String export(ExportDataRequest data) throws ExportException;

}