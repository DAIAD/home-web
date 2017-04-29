package eu.daiad.web.service.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.domain.application.SurveyEntity;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroMeasurement;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ExportErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.service.etl.model.EnumPhase;
import eu.daiad.web.service.etl.model.MemorySessionStore;
import eu.daiad.web.service.etl.model.Phase;
import eu.daiad.web.service.etl.model.PhaseTimeline;

/**
 * Service that exports amphiro b1 data for a utility.
 */
@Service
public class UtilityAmphiroDataExportService extends AbstractUtilityDataExportService {

    /**
     * Session property name for mobile OS.
     */
    private static final String SESSION_PROPERTY_OS = "settings.os";

    /**
     * Repository for accessing amphiro b1 readings.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    /**
     * Exports amphiro data for a single utility to a file. Any exported data file is replaced.
     *
     * @param query the query that selects the data to export.
     * @return the result of the export operation.
     * @throws ApplicationException if the query execution or file creation fails.
     */
    public ExportResult export(UtilityDataExportQuery query) throws ApplicationException {
        ExportResult result = new ExportResult();

        try {
            // Initialize directories
            if(StringUtils.isBlank(query.getWorkingDirectory())) {
               query.setWorkingDirectory(workingDirectory);
            }

            ensureDirectory(query.getWorkingDirectory());
            ensureDirectory(query.getTargetDirectory());

            // Set default time zone for the utility if not values is specified
            if(StringUtils.isBlank(query.getTimezone())) {
                query.setTimezone(query.getUtility().getTimezone());
            }

            // Set time zone
            ensureTimezone(query.getTimezone());

            // Set default file name
            if (StringUtils.isBlank(query.getFilename())) {
                query.setFilename(query.getUtility().getName());
            }

            // Export users
            exportUsers(query, result);

            // Export phases
            exportPhaseTimestamps(query, result);
            Map<UUID, Long> deviceMaxShowerId = new HashMap<UUID, Long>();
            exportPhaseSessionIndexes(query, result, deviceMaxShowerId);

            // Export sessions and measurements
            exportAmphiroSessionData(query, result, deviceMaxShowerId);
            exportAmphiroTimeSeries(query, result, deviceMaxShowerId);

            // Export errors
            exportMessages(query, result);

            return result;
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    /**
     * Exports users
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportUsers(UtilityDataExportQuery query, ExportResult result) throws IOException {
        long totalUsers = 0;

        String filename = createTemporaryFilename(query.getWorkingDirectory());

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter printer = new CSVPrinter(
                                new BufferedWriter(
                                    new OutputStreamWriter(
                                        new FileOutputStream(filename, true),
                                        Charset.forName("UTF-8").newEncoder())), format);


        ArrayList<String> row = new ArrayList<String>();

        row.add("user key");
        row.add("user name");

        printer.printRecord(row);

        for (SurveyEntity survey : userRepository.getSurveyDataByUtilityId(query.getUtility().getId())) {
            AuthenticatedUser user = userRepository.getUserByName(survey.getUsername());

            if (user != null) {
                row = new ArrayList<String>();

                row.add(user.getKey().toString());
                row.add(user.getUsername());

                totalUsers++;

                printer.printRecord(row);
            }
        }

        printer.flush();
        printer.close();

        result.increment(totalUsers);

        result.getFiles().add(new FileLabelPair(new File(filename), "user.csv", totalUsers));
    }

    /**
     * Export a single phase to a row.
     *
     * @param row the row to append phase data.
     * @param phase the phase to export.
     * @param formatter formatter for date/time properties.
     * @param interpolationError true if an error has occurred during the interpolation or shower index values.
     */
    private void createPhaseRowWithShowers(List<String> row, Phase phase, DateTimeFormatter formatter, boolean interpolationError) {
        row.add(phase.getPhaseLabel());
        if(phase.getMinSessionId() != null) {
            row.add(phase.getMinSessionId().toString());
        } else {
            if(!interpolationError) {
                row.add("");
            } else {
                row.add("N/A");
            }
        }
        if(phase.getMaxSessionId() != null) {
            row.add(phase.getMaxSessionId().toString());
        } else {
            if(!interpolationError) {
                row.add("");
            } else {
                row.add("N/A");
            }
        }
    }

    /**
     * Creates a CSV row for an amphiro session.
     *
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @param formatter the formatter for writing date values.
     * @param session the session to serialize.
     * @return a list of string tokens to be written to a CSV value.
     */
    private List<String> createAmphiroSessionRow(UUID userKey, UUID deviceKey, DateTimeFormatter formatter, AmphiroSession session) {
        return this.createAmphiroSessionRow(userKey, deviceKey, formatter, session, null);
    }

    /**
     * Creates a CSV row for an amphiro session.
     *
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @param formatter the formatter for writing date values.
     * @param session the session to serialize.
     * @param error validation error.
     * @return a list of string tokens to be written to a CSV value.
     */
    private List<String> createAmphiroSessionRow(UUID userKey, UUID deviceKey, DateTimeFormatter formatter, AmphiroSession session, String error) {
        List<String> row = new ArrayList<String>();

        row.add(userKey.toString());
        row.add(deviceKey.toString());
        row.add(Long.toString(session.getId()));
        row.add(session.getUtcDate().toString(formatter));
        row.add(Float.toString(session.getVolume()));
        row.add(Float.toString(session.getTemperature()));
        row.add(Float.toString(session.getEnergy()));
        row.add(Float.toString(session.getFlow()));
        row.add(Integer.toString(session.getDuration()));
        row.add(Boolean.toString(session.isHistory()));
        if (session.getMember() == null) {
            row.add("");
            row.add("");
        } else {
            row.add(session.getMember().getMode().toString());
            row.add(Integer.toString(session.getMember().getIndex()));
        }
        row.add(Boolean.toString(session.isIgnored()));
        if (session.getPropertyByKey(SESSION_PROPERTY_OS) == null) {
            row.add("");
        } else {
            row.add(session.getPropertyByKey("settings.os"));
        }
        if(error != null) {
            row.add(error);
        }

        return row;
    }

    /**
     * Creates a CSV row for an amphiro session.
     *
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @param formatter the formatter for writing date values.
     * @param session the session to serialize.
     * @return a list of string tokens to be written to a CSV value.
     */
    private List<String> createAmphiroMeasurementRow(UUID userKey,
                                                     UUID deviceKey,
                                                     DateTimeFormatter formatter,
                                                     AmphiroSession session,
                                                     AmphiroMeasurement measurement) {
        List<String> row = new ArrayList<String>();

        row.add(userKey.toString());
        row.add(deviceKey.toString());
        row.add(Long.toString(session.getId()));
        row.add(Long.toString(measurement.getIndex()));
        row.add(measurement.getUtcDate().toString(formatter));
        row.add(Float.toString(measurement.getVolume()));
        row.add(Float.toString(measurement.getTemperature()));
        row.add(Float.toString(measurement.getEnergy()));

        return row;
    }

    /**
     * Exports amphiro b1 data.
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @param deviceMaxShowerId a map with the max shower id for each device key.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportAmphiroSessionData(UtilityDataExportQuery query, ExportResult result, Map<UUID, Long> deviceMaxShowerId) throws IOException {
        long totalRows = 0;
        long totalValidRows = 0;
        long totalRemovedRows = 0;
        long totalIndexRows = 0;

        String allFilename = createTemporaryFilename(query.getWorkingDirectory());
        String validFilename = createTemporaryFilename(query.getWorkingDirectory());
        String removeFilename = createTemporaryFilename(query.getWorkingDirectory());
        String filterFilename = createTemporaryFilename(query.getWorkingDirectory());

        DateTimeFormatter formatter = DateTimeFormat.forPattern(query.getDateFormat()).withZone(DateTimeZone.forID(query.getTimezone()));

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter allPrinter = new CSVPrinter(
                                    new BufferedWriter(
                                        new OutputStreamWriter(
                                            new FileOutputStream(allFilename, true),
                                            Charset.forName("UTF-8").newEncoder())), format);

        CSVPrinter validPrinter = new CSVPrinter(
                                     new BufferedWriter(
                                         new OutputStreamWriter(
                                             new FileOutputStream(validFilename, true),
                                             Charset.forName("UTF-8").newEncoder())), format);

        CSVPrinter removedDataPrinter = new CSVPrinter(
                                            new BufferedWriter(
                                                new OutputStreamWriter(
                                                    new FileOutputStream(removeFilename, true),
                                                                         Charset.forName("UTF-8").newEncoder())), format);

        CSVPrinter removedIndexPrinter = new CSVPrinter(
                                             new BufferedWriter(
                                                 new OutputStreamWriter(
                                                     new FileOutputStream(filterFilename, true),
                                                                          Charset.forName("UTF-8").newEncoder())), format);

        // Write header for files "all", "valid" and "removed" showers
        List<String> row = new ArrayList<String>();

        row.add("user key");
        row.add("device key");
        row.add("session id");
        row.add(query.getUtility().getName() + " local date time");
        row.add("volume");
        row.add("temperature");
        row.add("energy");
        row.add("flow");
        row.add("duration");
        row.add("history");
        row.add("household member selection mode");
        row.add("household member index");
        row.add("ignore");
        row.add("mobile os");

        allPrinter.printRecord(row);
        validPrinter.printRecord(row);

        row.add("validation error");
        removedDataPrinter.printRecord(row);

        // Write header for file "removed" shower indexes
        row = new ArrayList<String>();

        row.add("user key");
        row.add("user name");
        row.add("device key");
        row.add("device name");

        removedIndexPrinter.printRecord(row);

        // Process all users
        for (SurveyEntity survey : userRepository.getSurveyDataByUtilityId(query.getUtility().getId())) {
            AuthenticatedUser user = userRepository.getUserByName(survey.getUsername());

            if (user != null) {
                // Get devices
                List<Device> devices = deviceRepository.getUserDevices(user.getKey(), new DeviceRegistrationQuery());

                List<String> deviceNames = new ArrayList<String>();
                List<UUID> deviceKeys = new ArrayList<UUID>();

                for (Device d : devices) {
                    if (d.getType() == EnumDeviceType.AMPHIRO) {
                        deviceKeys.add(d.getKey());
                        deviceNames.add(((AmphiroDevice) d).getName());
                    }
                }

                if (deviceKeys.size() == 0) {
                    continue;
                }

                // Get sessions
                AmphiroSessionCollectionIndexIntervalQuery sessionQuery = new AmphiroSessionCollectionIndexIntervalQuery();

                sessionQuery.setUserKey(user.getKey());
                sessionQuery.setDeviceKey(deviceKeys.toArray(new UUID[] {}));
                sessionQuery.setType(EnumIndexIntervalQuery.SLIDING);
                sessionQuery.setLength(Integer.MAX_VALUE);

                AmphiroSessionCollectionIndexIntervalQueryResult amphiroCollection = amphiroIndexOrderedRepository
                                .getSessions(deviceNames.toArray(new String[] {}), DateTimeZone.forID(query.getTimezone()), sessionQuery);

                // Process showers for every device
                for (AmphiroSessionCollection device : amphiroCollection.getDevices()) {
                    int total = device.getSessions().size();
                    Long maxId = deviceMaxShowerId.get(device.getDeviceKey());

                    List<AmphiroAbstractSession> sessions = device.getSessions();
                    List<AmphiroSession> removedSessions = new ArrayList<AmphiroSession>();
                    List<String> validationErrors = new ArrayList<String>();

                    // Remove sessions based on volume, duration and flow
                    for (int i = sessions.size() - 1; i >= 0; i--) {
                        AmphiroSession session = (AmphiroSession) sessions.get(i);
                        if ((maxId != null) && (session.getId() > maxId)) {
                            sessions.remove(session);
                            total--;
                            continue;
                        }

                        // Always export session to a file that contains all the
                        // session data
                        row = createAmphiroSessionRow(user.getKey(), device.getDeviceKey(), formatter, session);

                        allPrinter.printRecord(row);
                        totalRows++;

                        // Validate
                        if((session.getVolume() < 8) ||
                           (session.getDuration() < 60) ||
                           (session.getFlow() < 3)) {
                            // Remove from valid session collection ...
                            sessions.remove(session);
                            // ... and add to the removed session collection
                            removedSessions.add(session);

                            // Set error message
                            if (session.getVolume() < 8) {
                                validationErrors.add(getMessage(ExportErrorCode.VALIDATION_RULE_VOLUME_LESS, "volume", "8"));
                            } else if (session.getDuration() < 60) {
                                validationErrors.add(getMessage(ExportErrorCode.VALIDATION_RULE_DURATION_LESS, "duration", "60"));
                            } else if (session.getFlow() < 3) {
                                validationErrors.add(getMessage(ExportErrorCode.VALIDATION_RULE_FLOW_LESS, "flow", "3"));
                            }
                        }
                    }

                    // Remove sessions based on volume percentile and export data
                    float volumeThreshold = getVolumeThreshold(sessions, 5);

                    for (int i = sessions.size() - 1; i >= 0; i--) {
                        AmphiroSession session = (AmphiroSession) sessions.get(i);

                        if(session.getVolume() <= volumeThreshold) {
                            // Remove from valid session collection ...
                            sessions.remove(session);
                            // ... and add to the removed session collection
                            removedSessions.add(session);

                            validationErrors.add(getMessage(ExportErrorCode.VALIDATION_RULE_VOLUME_PERCENTILE, "percent", "5"));
                        } else {
                            row = createAmphiroSessionRow(user.getKey(), device.getDeviceKey(), formatter, session);

                            totalValidRows++;
                            validPrinter.printRecord(row);
                        }
                    }

                    // Export removed session
                    for (int r = 0, count = removedSessions.size(); r < count; r++) {
                        AmphiroSession session = removedSessions.get(r);

                        row = createAmphiroSessionRow(user.getKey(),
                                                      device.getDeviceKey(),
                                                      formatter,
                                                      session,
                                                      validationErrors.get(r));

                        totalRemovedRows++;
                        removedDataPrinter.printRecord(row);
                    }

                    // Export removed session indexes
                    if (removedSessions.size() > 0) {
                        row = new ArrayList<String>();

                        row.add(user.getKey().toString());
                        row.add(user.getUsername());
                        row.add(device.getDeviceKey().toString());
                        row.add(device.getName());

                        for(AmphiroSession session : removedSessions) {
                            row.add(Long.toString(session.getId()));
                        }

                        totalIndexRows++;
                        removedIndexPrinter.printRecord(row);
                    }

                    // Check if more than 30% of the sessions have been removed
                    if (removedSessions.size() > 0) {
                        if (((float) removedSessions.size() / total) > 0.3) {
                            result.addMessage(user.getKey(),
                                              user.getUsername(),
                                              device.getDeviceKey(),
                                              getMessage(ExportErrorCode.VALIDATION_RULE_TOO_MANY_RECORDS_REMOVED,
                                                         "percent",
                                                         "30",
                                                         "total",
                                                         Integer.toString(total),
                                                         "removed",
                                                         Integer.toString(removedSessions.size())));
                        }
                    }
                }
            }
        }

        allPrinter.flush();
        allPrinter.close();

        validPrinter.flush();
        validPrinter.close();

        removedDataPrinter.flush();
        removedDataPrinter.close();

        removedIndexPrinter.flush();
        removedIndexPrinter.close();

        result.increment(totalValidRows + totalRemovedRows + totalIndexRows);

        result.getFiles().add(new FileLabelPair(new File(allFilename), "shower-data-all.csv", totalRows));
        result.getFiles().add(new FileLabelPair(new File(validFilename), "shower-data-valid.csv", totalValidRows));
        result.getFiles().add(new FileLabelPair(new File(removeFilename), "shower-data-removed.csv", totalRemovedRows));
        result.getFiles().add(new FileLabelPair(new File(filterFilename), "shower-data-removed-index.csv", totalIndexRows));
    }

    /**
     * Exports amphiro b1 time series.
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @param deviceMaxShowerId a map with the max shower id for each device key.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportAmphiroTimeSeries(UtilityDataExportQuery query, ExportResult result, Map<UUID, Long> deviceMaxShowerId) throws IOException {
        long totalRows = 0;

        String dataFilename = createTemporaryFilename(query.getWorkingDirectory());

        DateTimeFormatter formatter = DateTimeFormat.forPattern(query.getDateFormat()).withZone(DateTimeZone.forID(query.getTimezone()));

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter dataPrinter = new CSVPrinter(
                                     new BufferedWriter(
                                         new OutputStreamWriter(
                                             new FileOutputStream(dataFilename, true),
                                             Charset.forName("UTF-8").newEncoder())), format);

        // Write headers
        List<String> row = new ArrayList<String>();

        row.add("user key");
        row.add("device key");
        row.add("session id");
        row.add("measurement index");
        row.add(query.getUtility().getName() + " local date time");
        row.add("volume");
        row.add("temperature");
        row.add("energy");

        dataPrinter.printRecord(row);

        // Process all users
        for (SurveyEntity survey : userRepository.getSurveyDataByUtilityId(query.getUtility().getId())) {
            AuthenticatedUser user = userRepository.getUserByName(survey.getUsername());

            if (user != null) {
                // Get devices
                List<Device> devices = deviceRepository.getUserDevices(user.getKey(), new DeviceRegistrationQuery());

                List<String> deviceNames = new ArrayList<String>();
                List<UUID> deviceKeys = new ArrayList<UUID>();

                for (Device d : devices) {
                    if (d.getType() == EnumDeviceType.AMPHIRO) {
                        deviceKeys.add(d.getKey());
                        deviceNames.add(((AmphiroDevice) d).getName());
                    }
                }

                if (deviceKeys.size() == 0) {
                    continue;
                }

                // Get sessions
                AmphiroSessionCollectionIndexIntervalQuery userSessionQuery = new AmphiroSessionCollectionIndexIntervalQuery();

                userSessionQuery.setUserKey(user.getKey());
                userSessionQuery.setDeviceKey(deviceKeys.toArray(new UUID[] {}));
                userSessionQuery.setType(EnumIndexIntervalQuery.SLIDING);
                userSessionQuery.setLength(Integer.MAX_VALUE);

                AmphiroSessionCollectionIndexIntervalQueryResult amphiroCollection = amphiroIndexOrderedRepository
                                .getSessions(deviceNames.toArray(new String[] {}), DateTimeZone.forID(query.getTimezone()), userSessionQuery);

                // Process showers for every device and extract time series for real time ones.
                for (AmphiroSessionCollection device : amphiroCollection.getDevices()) {
                    Long maxId = deviceMaxShowerId.get(device.getDeviceKey());

                    for (AmphiroAbstractSession session : device.getSessions()) {
                        AmphiroSession amphiroSession = (AmphiroSession) session;
                        if ((maxId != null) && (amphiroSession.getId() > maxId)) {
                            continue;
                        }

                        if(!amphiroSession.isHistory()) {
                            AmphiroSessionIndexIntervalQuery sessionQuery =
                                new AmphiroSessionIndexIntervalQuery(user.getKey(), device.getDeviceKey(), amphiroSession.getId());

                            AmphiroSessionIndexIntervalQueryResult sessionResult = amphiroIndexOrderedRepository.getSession(sessionQuery);

                            for(AmphiroMeasurement measurement : sessionResult.getSession().getMeasurements()) {
                                row = createAmphiroMeasurementRow(user.getKey(),
                                                                  device.getDeviceKey(),
                                                                  formatter,
                                                                  amphiroSession,
                                                                  measurement);

                                totalRows++;
                                dataPrinter.printRecord(row);
                            }
                        }
                    }
                }
            }
        }

        dataPrinter.flush();
        dataPrinter.close();

        result.increment(totalRows);

        result.getFiles().add(new FileLabelPair(new File(dataFilename), "shower-time-series.csv", totalRows));
    }

    /**
     * Exports messages
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @throws IOException if file creation fails.
     */
    private void exportMessages(UtilityDataExportQuery query, ExportResult result) throws IOException {
        String filename = createTemporaryFilename(query.getWorkingDirectory());

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter printer = new CSVPrinter(
                                new BufferedWriter(
                                    new OutputStreamWriter(
                                        new FileOutputStream(filename, true),
                                        Charset.forName("UTF-8").newEncoder())), format);

        // Write header
        ArrayList<String> row = new ArrayList<String>();

        row.add("user key");
        row.add("user name");
        row.add("device key");
        row.add("message");

        printer.printRecord(row);

        for(ExportResult.Message message : result.getMessages()) {
            row = new ArrayList<String>();

            row.add(message.getUserKey().toString());
            row.add(message.getUsername());
            row.add(message.getDeviceKey().toString());
            row.add(message.getMessage());

            printer.printRecord(row);
        }

        printer.flush();
        printer.close();

        result.getFiles().add(new FileLabelPair(new File(filename), "error.csv", result.getMessages().size()));
    }

    /**
     * Exports phase start/end session index for amphiro b1.
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @param deviceMaxShowerId a map with the max shower id for each device key.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportPhaseSessionIndexes(UtilityDataExportQuery query, ExportResult result, Map<UUID, Long> deviceMaxShowerId) throws IOException {
        long totalRows = 0;

        String filename = createTemporaryFilename(query.getWorkingDirectory());

        DateTimeFormatter formatter = DateTimeFormat.forPattern(query.getDateFormat()).withZone(DateTimeZone.forID(query.getTimezone()));

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter printer = new CSVPrinter(
                                new BufferedWriter(
                                    new OutputStreamWriter(
                                        new FileOutputStream(filename, true),
                                        Charset.forName("UTF-8").newEncoder())), format);

        // Write header
        ArrayList<String> row = new ArrayList<String>();

        row.add("user key");
        row.add("user name");
        row.add("device key");
        row.add("device name");

        row.add("BASELINE");
        row.add("BASELINE start");
        row.add("BASELINE end");

        row.add("Phase 1");
        row.add("Phase 1 start");
        row.add("Phase 1 end");

        row.add("Phase 2");
        row.add("Phase 2 start");
        row.add("Phase 2 end");

        row.add("Phase 3");
        row.add("Phase 3 start");
        row.add("Phase 3 end");

        if (query.isExportFinalTrialData()) {
            row.add("Phase 4");
            row.add("Phase 4 start");
            row.add("Phase 4 end");
        }

        printer.printRecord(row);

        for (SurveyEntity survey : userRepository.getSurveyDataByUtilityId(query.getUtility().getId())) {
            AuthenticatedUser user = userRepository.getUserByName(survey.getUsername());

            if (user != null) {
                // Get devices
                List<Device> devices = deviceRepository.getUserDevices(user.getKey(), new DeviceRegistrationQuery());

                List<String> deviceNames = new ArrayList<String>();
                List<UUID> deviceKeys = new ArrayList<UUID>();

                for (Device d : devices) {
                    if (d.getType() == EnumDeviceType.AMPHIRO) {
                        deviceKeys.add(d.getKey());
                        deviceNames.add(((AmphiroDevice) d).getName());
                    }
                }

                if (deviceKeys.size() == 0) {
                    continue;
                }

                AmphiroSessionCollectionIndexIntervalQuery sessionQuery = new AmphiroSessionCollectionIndexIntervalQuery();

                sessionQuery.setUserKey(user.getKey());
                sessionQuery.setDeviceKey(deviceKeys.toArray(new UUID[] {}));
                sessionQuery.setType(EnumIndexIntervalQuery.SLIDING);
                sessionQuery.setLength(Integer.MAX_VALUE);

                AmphiroSessionCollectionIndexIntervalQueryResult amphiroCollection = amphiroIndexOrderedRepository
                                .getSessions(deviceNames.toArray(new String[] {}), DateTimeZone.forID(query.getTimezone()), sessionQuery);

                for (AmphiroSessionCollection device : amphiroCollection.getDevices()) {
                    try {
                        MemorySessionStore sessions = new MemorySessionStore(device.getSessions());

                        // Remove any historical sessions before the pairing. Such
                        // sessions may include showers before the pairing or failed
                        // pairing attempts.
                        sessions.cleanPairingSessions();

                        if(sessions.isEmpty()) {
                            continue;
                        }

                        // Get timeline
                        PhaseTimeline timeline = constructAmphiroPhaseTimeline(user.getKey(), device.getDeviceKey());
                        if(query.isExportFinalTrialData()) {
                            timeline.overrideDates(DateTimeZone.forID(query.getTimezone()));
                        }

                        boolean interpolationError = false;
                        for (int i = 0, total = timeline.size(); i < total; i++) {
                            Phase phase = timeline.get(i);

                            if (phase.getPhase() == EnumPhase.EMPTY) {
                                continue;
                            }
                            if(!interpolationError) {
                                switch(phase.getLeftBoundSelection()) {
                                    case NEAREST:
                                        AmphiroSession nearest = sessions.getNearestSession(phase.getStartTimestamp());

                                        if (nearest == null) {
                                            throw new RuntimeException(String.format("Failed to find the session index associated with phase [%s]. Total sessions [%d]. Timeline : %s",
                                                                                     phase.getPhase(),
                                                                                     sessions.size(),
                                                                                     timeline));
                                        }

                                        phase.setMinSessionId(nearest.getId());
                                        break;
                                    case INTERPOLATION:
                                        long[] indexes = sessions.interpolate(phase.getStartTimestamp(),
                                                                              phase.getPrevious().getDays(),
                                                                              phase.getDays(),
                                                                              survey.getHouseholdMemberTotal(),
                                                                              survey.getShowersPerWeek(),
                                                                              devices.size());
                                          if (indexes == null) {
                                              String message = String.format("Failed to interpolate session indexes for phase [%s]. Total sessions [%d]. Timeline : %s",
                                                                             phase.getPhase(),
                                                                             sessions.size(),
                                                                             timeline);

                                              result.addMessage(user.getKey(),
                                                               user.getUsername(),
                                                               device.getDeviceKey(),
                                                               String.format("Cannot compute shower indexes for all phases for user [%s]: %s", user.getUsername(), message));

                                              interpolationError = true;
                                             break;
                                          }

                                          phase.setMinSessionId(indexes[1]);
                                        break;
                                    case MIN:
                                        phase.setMinSessionId(sessions.get(0).getId());
                                        break;
                                    default:
                                        throw new RuntimeException(String.format("Cannot resolve left bound for phase [%s].", phase.getPhase()));
                                }
                            }
                            if(!interpolationError) {
                                switch(phase.getRightBoundSelection()) {
                                    case NEAREST:
                                        AmphiroSession nearest;
                                        if ((phase.getNext() == null) && (i == 3) && (query.isExportFinalTrialData())) {
                                            // Handle special case next phase does not exist
                                            nearest= sessions.getNearestSession((new DateTime(2017, 2, 1, 0, 0, DateTimeZone.forID(query.getTimezone()))).getMillis());
                                        } else {
                                            nearest= sessions.getNearestSession(phase.getNext().getStartTimestamp());
                                        }

                                        if (nearest == null) {
                                            throw new RuntimeException(String.format("Failed to find the session index associated with phase [%s]. Total sessions [%d]. Timeline : %s",
                                                                                     phase.getPhase(),
                                                                                     sessions.size(),
                                                                                     timeline));
                                        }

                                        Long previousId = sessions.getBefore(nearest.getId());
                                        if (previousId == null) {
                                            phase.invalidate();
                                            continue;
                                        } else {
                                            phase.setMaxSessionId(previousId);
                                        }
                                        break;
                                    case INTERPOLATION:
                                        long[] indexes = sessions.interpolate(phase.getEndTimestamp(),
                                                                              phase.getDays(),
                                                                              phase.getNext().getDays(),
                                                                              survey.getHouseholdMemberTotal(),
                                                                              survey.getShowersPerWeek(),
                                                                              devices.size());
                                        if (indexes == null) {
                                             String message = String.format("Failed to interpolate session indexes for phase [%s]. Total sessions [%d]. Timeline : %s",
                                                            phase.getPhase(),
                                                            sessions.size(),
                                                            timeline);

                                             result.addMessage(user.getKey(),
                                                              user.getUsername(),
                                                              device.getDeviceKey(),
                                                              String.format("Cannot to compute shower indexes for all phases for user [%s]: %s", user.getUsername(), message));

                                            interpolationError = true;
                                            phase.setMaxSessionId(sessions.get(sessions.size() - 1).getId());
                                            break;
                                        }

                                        phase.setMaxSessionId(indexes[0]);
                                        break;
                                    case MAX:
                                        phase.setMaxSessionId(sessions.get(sessions.size() - 1).getId());
                                        break;
                                    default:
                                        throw new RuntimeException(String.format("Cannot resolve right bound for phase [%s].", phase.getPhase()));
                                }
                            }
                            if(interpolationError) {
                                break;
                            }
                        }

                        timeline.validate();

                        row = new ArrayList<String>();

                        row.add(user.getKey().toString());
                        row.add(user.getUsername());
                        row.add(device.getDeviceKey().toString());
                        row.add(device.getName());

                        for (int i = 0, size = timeline.size(); i < size; i++) {
                            Phase phase = timeline.get(i);

                            if (phase.getPhase() == EnumPhase.EMPTY) {
                                row.add("");
                                row.add("");
                                row.add("");
                            } else {
                                createPhaseRowWithShowers(row, phase, formatter, interpolationError);
                                if(query.isExportFinalTrialData()) {
                                    deviceMaxShowerId.put(device.getDeviceKey(), phase.getMaxSessionId());
                                }
                            }
                        }
                        // Add empty entries
                        for (int i = timeline.size(); i < 4; i++) {
                            row.add("");
                            row.add("");
                            row.add("");
                        }
                        // Add extra phase
                        if(query.isExportFinalTrialData()) {
                            row.add("AMPHIRO_ON_MOBILE_ON_SOCIAL_ON");
                            if((interpolationError) || (timeline.size() != 4)) {
                                row.add("N/A");
                                row.add("N/A");
                            } else {
                                long startTimestamp = (new DateTime(2017, 2, 1, 0, 0, DateTimeZone.forID(query.getTimezone()))).getMillis();
                                long endTimestamp = (new DateTime(2017, 3, 1, 0, 0, DateTimeZone.forID(query.getTimezone()))).getMillis();

                                AmphiroSession left = sessions.getNearestSession(startTimestamp);
                                AmphiroSession right = sessions.getNearestSession(endTimestamp);

                                if ((left != null) && (right != null) && (left.getId() <= right.getId())) {
                                    row.add(Long.toString(left.getId()));
                                    row.add(Long.toString(right.getId()));

                                    if(query.isExportFinalTrialData()) {
                                        deviceMaxShowerId.put(device.getDeviceKey(), right.getId());
                                    }
                                } else {
                                    row.add("N/A");
                                    row.add("N/A");
                                }
                            }
                        }
                        totalRows++;

                        printer.printRecord(row);
                    } catch(Exception ex) {
                        result.addMessage(user.getKey(),
                                          user.getUsername(),
                                          device.getDeviceKey(),
                                          String.format("Failed to export shower id phase timeline for user [%s]: %s",
                                                        user.getUsername(),
                                                        ex.getMessage()));
                    }
                }
            }
        }

        printer.flush();
        printer.close();

        result.increment(totalRows);
        result.getFiles().add(new FileLabelPair(new File(filename), "phase-shower-id.csv", totalRows));
    }

    /**
     * For a list of showers, find the volume threshold for the given
     * percentile based on volume.
     *
     * @param sessions the list of sessions.
     * @param percentile the percentile value.
     * @return the volume threshold.
     */
    private float getVolumeThreshold(List<AmphiroAbstractSession> sessions, float percentile) {
        if(sessions.size() == 0) {
            return 0;
        }

        // Order by volume
        Collections.sort(sessions, new Comparator<AmphiroAbstractSession>() {
            @Override
            public int compare(AmphiroAbstractSession s1, AmphiroAbstractSession s2) {
                if (s1.getVolume() < s2.getVolume()) {
                    return -1;
                }
                if (s1.getVolume() > s2.getVolume()) {
                    return 1;
                }
                return 0;
            }
        });

        int index = Math.round(sessions.size() * percentile / 100);
        if (index >= sessions.size()) {
            index = sessions.size() - 1;
        }

        float volume = sessions.get(index).getVolume();

        // Restore order based on session id
        Collections.sort(sessions, new Comparator<AmphiroAbstractSession>() {
            @Override
            public int compare(AmphiroAbstractSession s1, AmphiroAbstractSession s2) {
                if (((AmphiroSession) s1).getId() <= ((AmphiroSession) s2).getId()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return volume;
    }
}
