package eu.daiad.web.service.etl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.springframework.stereotype.Service;

import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.meter.WaterMeterDataPoint;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IMeterDataRepository;

/**
 * Service for exporting data for a single user.
 */
@Service
public class UserDataExportService extends AbstractDataExportService {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(UserDataExportService.class);

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing device (smart water meter or amphiro b1) data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Repository for accessing amphiro b1 readings.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    /**
     * Repository for accessing smart water meter readings.
     */
    @Autowired
    private IMeterDataRepository waterMeterMeasurementRepository;

    /**
     * Exports data for a single user to a temporary file.
     *
     * @param query the query that selects the data to export.
     * @return the result of the export operation.
     *
     * @throws ApplicationException if the query execution or file creation fails.
     */
    public ExportResult export(UserDataExportQuery query) throws ApplicationException {
        ExportResult result = new ExportResult();

        XSSFWorkbook workbook = null;

        try {
            // Initialize working directory
            ensureWorkingDirectory();

            File excelFile = new File(createTemporaryFilename(workingDirectory));

            // Get user
            AuthenticatedUser user = userRepository.getUserByKey(query.getUserKey());

            // Check time zone
            if (StringUtils.isBlank(query.getTimezone())) {
                query.setTimezone(user.getTimezone());
            }
            ensureTimezone(query.getTimezone());

            // Get user devices
            List<Device> devices = deviceRepository.getUserDevices(query.getUserKey(), new DeviceRegistrationQuery());

            List<String> amphiroName = new ArrayList<String>();
            List<UUID> amphiroKey = new ArrayList<UUID>();

            List<String> meterSerial = new ArrayList<String>();
            List<UUID> meterKey = new ArrayList<UUID>();

            for (Device d : devices) {
                boolean fetch = false;

                if ((query.getDeviceKeys() == null) || (query.getDeviceKeys().length == 0)) {
                    fetch = true;
                } else {
                    for (UUID deviceKey : query.getDeviceKeys()) {
                        if (d.getKey().equals(deviceKey)) {
                            fetch = true;
                            break;
                        }
                    }
                }

                if (fetch) {
                    switch (d.getType()) {
                        case AMPHIRO:
                            amphiroKey.add(d.getKey());
                            amphiroName.add(((AmphiroDevice) d).getName());

                            break;
                        case METER:
                            meterKey.add(d.getKey());
                            meterSerial.add(((WaterMeterDevice) d).getSerial());

                            break;
                        default:
                            // Ignore device
                    }
                }
            }

            // Initialize excel work book
            workbook = new XSSFWorkbook();

            // Load sessions
            result.increment(exportUserAmphiroDataToExcel(workbook,
                                                         query.getUserKey(),
                                                         amphiroKey.toArray(new UUID[amphiroKey.size()]),
                                                         amphiroName.toArray(new String[amphiroName.size()]),
                                                         query.getTimezone(),
                                                         "yyyy-MM-dd HH:mm:ss"));

            // Load readings
            result.increment(exportUserMeterDataToExcel(workbook,
                                                        query.getUserKey(),
                                                        meterKey.toArray(new UUID[meterKey.size()]),
                                                        meterSerial.toArray(new String[meterSerial.size()]),
                                                        query.getTimezone(),
                                                        "yyyy-MM-dd HH:mm:ss"));

            // Write workbook
            FileOutputStream excelFileStream = new FileOutputStream(excelFile);
            workbook.write(excelFileStream);
            excelFileStream.close();
            excelFileStream = null;

            result.getFiles().add(new FileLabelPair(excelFile, user.getUsername() + ".xlsx", result.getTotalRows()));

            return result;
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
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


    /**
     * Exports amphiro b1 sessions.
     *
     * @param workbook the EXCEL workbook to write to.
     * @param userKey the unique user key (UUID).
     * @param deviceKeys the unique device keys (UUID).
     * @param deviceNames the user friendly device names.
     * @param timezone the output time zone.
     * @param dateFormat date format pattern.
     * @return the number of rows exported.
     */
    private long exportUserAmphiroDataToExcel(XSSFWorkbook workbook, UUID userKey, UUID[] deviceKeys, String[] deviceNames, String timezone, String dateFormat) {
        long totalRows = 0;

        // Configure date time format
        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withZone(DateTimeZone.forID(timezone));

        // Load data
        AmphiroSessionCollectionIndexIntervalQuery query = new AmphiroSessionCollectionIndexIntervalQuery();

        query.setUserKey(userKey);
        query.setDeviceKey(deviceKeys);
        query.setType(EnumIndexIntervalQuery.SLIDING);
        query.setLength(Integer.MAX_VALUE);

        AmphiroSessionCollectionIndexIntervalQueryResult amphiroCollection = this.amphiroIndexOrderedRepository
                        .getSessions(deviceNames, DateTimeZone.forID(timezone), query);

        // Create one sheet per device
        for (AmphiroSessionCollection device : amphiroCollection.getDevices()) {
            int rowIndex = 0;

            String sheetName = device.getName();
            if (StringUtils.isBlank(sheetName)) {
                sheetName = device.getDeviceKey().toString();
            }

            sheetName = Integer.toString(workbook.getNumberOfSheets() + 1) + " - " + sheetName;

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
                totalRows++;

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

        return totalRows;
    }

    /**
     * Exports amphiro b1 sessions.
     *
     * @param workbook the EXCEL workbook to write to.
     * @param userKey the unique user key (UUID).
     * @param meterKeys the unique meter keys (UUID).
     * @param meterSerials the unique meter serial numbers.
     * @param timezone the output time zone.
     * @param dateFormat date format pattern.
     * @return the number of rows exported.
     */
    private long exportUserMeterDataToExcel(XSSFWorkbook workbook,
                                            UUID userKey,
                                            UUID[] meterKeys,
                                            String[] meterSerials,
                                            String timezone,
                                            String dateFormat) {
        long totalRows = 0;

        // Configure date time format
        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withZone(DateTimeZone.forID(timezone));

        // Load data
        WaterMeterMeasurementQuery query = new WaterMeterMeasurementQuery();

        query.setUserKey(userKey);
        query.setDeviceKey(meterKeys);
        query.setGranularity(TemporalConstants.NONE);
        query.setStartDate(null);
        query.setEndDate(null);

        WaterMeterMeasurementQueryResult meterCollection = this.waterMeterMeasurementRepository.searchMeasurements(
                        meterSerials, DateTimeZone.forID(timezone), query);

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
                totalRows++;

                row.createCell(0).setCellValue(point.getUtcDate().toString(formatter));
                row.createCell(1).setCellValue(point.getVolume());
                row.getCell(1).setCellStyle(style);
                row.createCell(2).setCellValue(point.getDifference());
                row.getCell(2).setCellStyle(style);
            }
        }

        return totalRows;
    }

}
