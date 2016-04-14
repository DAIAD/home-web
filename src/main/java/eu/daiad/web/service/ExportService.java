package eu.daiad.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQueryResult;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ExportErrorCode;
import eu.daiad.web.model.export.ExportUserDataQuery;
import eu.daiad.web.model.meter.WaterMeterDataPoint;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.repository.application.IAmphiroMeasurementRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

@Service
public class ExportService implements IExportService {

	private static final Log logger = LogFactory.getLog(ExportService.class);

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private IAmphiroMeasurementRepository amphiroMeasurementRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@Override
	public String export(ExportUserDataQuery query) throws ApplicationException {
		XSSFWorkbook workbook = null;

		try {
			// Create output folder
			String outputFolderName = this.temporaryPath;

			File outputFolder = new File(this.temporaryPath);

			outputFolder.mkdirs();

			if (!outputFolder.exists()) {
				throw new ApplicationException(ExportErrorCode.PATH_CREATION_FAILED).set("path", outputFolderName);
			}

			// Create new file name
			String token = UUID.randomUUID().toString();

			File excelFile = new File(FilenameUtils.concat(outputFolderName, token + ".xlsx"));
			File zipFile = new File(FilenameUtils.concat(outputFolderName, token + ".zip"));

			// Set time zone
			Set<String> zones = DateTimeZone.getAvailableIDs();
			if (query.getTimezone() == null) {
				query.setTimezone("Europe/Athens");
			}
			if (!zones.contains(query.getTimezone())) {
				throw new ApplicationException(ExportErrorCode.TIMEZONE_NOT_FOUND).set("timezone", query.getTimezone());
			}

			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(
							DateTimeZone.forID(query.getTimezone()));

			// Initialize excel work book
			workbook = new XSSFWorkbook();

			// Load sessions
			String[] nameArray = new String[query.getAmphiroNames().size()];
			query.getAmphiroNames().toArray(nameArray);

			UUID[] amphiroArray = new UUID[query.getAmphiroKeys().size()];
			query.getAmphiroKeys().toArray(amphiroArray);

			AmphiroSessionCollectionQuery sessionQuery = new AmphiroSessionCollectionQuery();
			sessionQuery.setUserKey(query.getUserKey());
			sessionQuery.setDeviceKey(amphiroArray);
			sessionQuery.setGranularity(TemporalConstants.NONE);
			sessionQuery.setStartDate(null);
			sessionQuery.setEndDate(null);

			AmphiroSessionCollectionQueryResult amphiroCollection = this.amphiroMeasurementRepository.searchSessions(
							nameArray, sessionQuery);

			// Create one sheet per device
			for (AmphiroSessionCollection device : amphiroCollection.getDevices()) {
				int rowIndex = 0;

				String sheetName = device.getName();
				if (StringUtils.isBlank(sheetName)) {
					sheetName = device.getDeviceKey().toString();
				}

				XSSFSheet sheet = workbook.createSheet(sheetName);

				// Write header
				Row row = sheet.createRow(rowIndex++);

				row.createCell(0).setCellValue("Session Id");
				row.createCell(1).setCellValue("Date Time");
				row.createCell(2).setCellValue("Volume");
				row.createCell(3).setCellValue("Temperature");
				row.createCell(4).setCellValue("Energy");
				row.createCell(5).setCellValue("Flow");
				row.createCell(6).setCellValue("Duration");
				row.createCell(7).setCellValue("History");

				DataFormat format = workbook.createDataFormat();
				CellStyle style = workbook.createCellStyle();

				style.setDataFormat(format.getFormat("0.00"));

				for (AmphiroAbstractSession session : device.getSessions()) {
					row = sheet.createRow(rowIndex++);

					row.createCell(0).setCellValue(((AmphiroSession) session).getId());
					row.createCell(1).setCellValue(session.getUtcDate().toString(formatter));

					row.createCell(2).setCellValue(session.getVolume());
					row.getCell(2).setCellStyle(style);
					row.createCell(3).setCellValue(session.getTemperature());
					row.getCell(3).setCellStyle(style);
					row.createCell(4).setCellValue(session.getEnergy());
					row.getCell(4).setCellStyle(style);
					row.createCell(5).setCellValue(session.getFlow());
					row.getCell(5).setCellStyle(style);

					row.createCell(6).setCellValue(session.getDuration());

					row.createCell(7).setCellValue(((AmphiroSession) session).isHistory() ? "YES" : "NO");
				}

			}

			// Load meter measurements
			String[] serialArray = new String[query.getMeterNames().size()];
			query.getMeterNames().toArray(serialArray);

			UUID[] meterArray = new UUID[query.getMeterKeys().size()];
			query.getMeterKeys().toArray(meterArray);

			WaterMeterMeasurementQuery meterQuery = new WaterMeterMeasurementQuery();
			meterQuery.setUserKey(query.getUserKey());
			meterQuery.setDeviceKey(meterArray);
			meterQuery.setGranularity(TemporalConstants.NONE);
			meterQuery.setStartDate(null);
			meterQuery.setEndDate(null);

			WaterMeterMeasurementQueryResult meterCollection = this.waterMeterMeasurementRepository.searchMeasurements(
							serialArray, meterQuery);

			// Create one sheet per device
			for (WaterMeterDataSeries series : meterCollection.getSeries()) {
				int rowIndex = 0;

				String sheetName = series.getSerial();
				if (StringUtils.isBlank(sheetName)) {
					sheetName = series.getDeviceKey().toString();
				}

				XSSFSheet sheet = workbook.createSheet(sheetName);

				// Write header
				Row row = sheet.createRow(rowIndex++);

				row.createCell(0).setCellValue("Date Time");
				row.createCell(1).setCellValue("Volume");
				row.createCell(2).setCellValue("Difference");

				DataFormat format = workbook.createDataFormat();
				CellStyle style = workbook.createCellStyle();

				style.setDataFormat(format.getFormat("0.00"));

				for (WaterMeterDataPoint point : series.getValues()) {
					row = sheet.createRow(rowIndex++);

					row.createCell(0).setCellValue(point.getUtcDate().toString(formatter));
					row.createCell(1).setCellValue(point.getVolume());
					row.getCell(1).setCellStyle(style);
					row.createCell(2).setCellValue(point.getDifference());
					row.getCell(2).setCellStyle(style);
				}
			}

			// Write workbook
			FileOutputStream excelFileStream = new FileOutputStream(excelFile);
			workbook.write(excelFileStream);
			excelFileStream.close();
			excelFileStream = null;

			// Compress file
			byte[] buffer = new byte[4096];

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			int len;

			ZipEntry ze = new ZipEntry(query.getUsername() + ".xlsx");
			zos.putNextEntry(ze);
			FileInputStream in = new FileInputStream(excelFile);
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			in.close();
			zos.flush();
			zos.closeEntry();

			zos.close();

			excelFile.delete();

			return token;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex);
		} finally {
			try {
				if (workbook != null) {
					workbook.close();
					workbook = null;
				}
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
	}
}
