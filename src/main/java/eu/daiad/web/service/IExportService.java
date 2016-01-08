package eu.daiad.web.service;

import eu.daiad.web.model.ExportData;
import eu.daiad.web.model.ExportException;

public interface IExportService {

	public abstract String export(ExportData data) throws ExportException;

}