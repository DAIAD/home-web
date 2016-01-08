package eu.daiad.web.service;

import eu.daiad.web.model.ExportException;
import eu.daiad.web.model.export.ExportData;

public interface IExportService {

	public abstract String export(ExportData data) throws ExportException;

}