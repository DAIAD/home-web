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
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
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
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;

/**
 * Service that exports amphiro b1 data for a utility.
 */
@Service
public class UtilityAmphiroDataExportService extends AbstractUtilityDataExportService {

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
            exportPhaseSessionIndexes(query, result);

            // Export sessions and measurements
            exportAmphiroSessionData(query, result);
            exportAmphiroTimeSeries(query, result);

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

        // Export data for every user
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
     * @param type type of the phase to export.
     * @param row the row to append phase data.
     * @param phase the phase to export.
     * @param formatter formatter for date/time properties.
     */
    private void createPhaseRowWithShowers(EnumPhase type, List<String> row, Phase phase, DateTimeFormatter formatter) {
        if(phase == null) {
            row.add(type.toString());
            row.add("");
            row.add("");
        } else {
            row.add(phase.getPhase().toString());
            if(phase.getMinSessionId() != null) {
                row.add(phase.getMinSessionId().toString());
            } else {
                row.add("");
            }
            if(phase.getMaxSessionId() != null) {
                row.add(phase.getMaxSessionId().toString());
            } else {
                row.add("");
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
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportAmphiroSessionData(UtilityDataExportQuery query, ExportResult result) throws IOException {
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

        // Write headers
        List<String> row = new ArrayList<String>();

        row.add("user key");
        row.add("device key");
        row.add("session id");
        row.add("local datetime");
        row.add("volume");
        row.add("temperature");
        row.add("energy");
        row.add("flow");
        row.add("duration");
        row.add("history");
        row.add("household member selection mode");
        row.add("household member index");
        row.add("ignore");

        allPrinter.printRecord(row);
        validPrinter.printRecord(row);
        removedDataPrinter.printRecord(row);

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

                    List<AmphiroAbstractSession> sessions = device.getSessions();
                    List<AmphiroSession> removedSessions = new ArrayList<AmphiroSession>();

                    // Remove sessions based on volume, duration and flow
                    for (int i = sessions.size() - 1; i >= 0; i--) {
                        AmphiroSession session = (AmphiroSession) sessions.get(i);

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
                        }
                    }

                    // Remove sessions based on volume percentile and export data
                    float volumeThreshold = getVolumeThreshold(sessions, 5);

                    for (int i = sessions.size() - 1; i >= 0; i--) {
                        AmphiroSession session = (AmphiroSession) sessions.get(i);

                        row = createAmphiroSessionRow(user.getKey(), device.getDeviceKey(), formatter, session);

                        if(session.getVolume() <= volumeThreshold) {
                            // Remove from valid session collection ...
                            sessions.remove(session);
                            // ... and add to the removed session collection
                            removedSessions.add(session);
                        } else {
                            totalValidRows++;
                            validPrinter.printRecord(row);
                        }
                    }

                    // Export removed session
                    for(AmphiroSession session : removedSessions) {
                        row = createAmphiroSessionRow(user.getKey(), device.getDeviceKey(), formatter, session);

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
                                              String.format("More than 30%% of showers has been removed. Total [%d]. Removed [%d]",
                                                            total,
                                                            removedSessions.size()));
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
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportAmphiroTimeSeries(UtilityDataExportQuery query, ExportResult result) throws IOException {
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
        row.add("local datetime");
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
                    for (AmphiroAbstractSession session : device.getSessions()) {
                        AmphiroSession amphiroSession = (AmphiroSession) session;

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
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportPhaseSessionIndexes(UtilityDataExportQuery query, ExportResult result) throws IOException {
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
                        SessionCollection sessions = new SessionCollection(device.getSessions());

                        // Remove any historical sessions before the pairing. Such
                        // sessions may include showers before the pairing or failed
                        // pairing attempts.
                        sessions.cleanPairingSessions();

                        if(sessions.isEmpty()) {
                            continue;
                        }

                        // Get timeline
                        PhaseTimeline phaseTimeline = constructAmphiroPhaseTimeline(user.getKey(), device.getDeviceKey());

                        Phase phase1 = phaseTimeline.getPhase(EnumPhase.BASELINE);
                        Phase phase2a = phaseTimeline.getPhase(EnumPhase.MOBILE_ON_AMPHIRO_OFF);
                        Phase phase2b = phaseTimeline.getPhase(EnumPhase.MOBILE_OFF_AMPHIRO_ON);
                        Phase phase3 = phaseTimeline.getPhase(EnumPhase.MOBILE_ON_AMPHIRO_ON);

                        if (phase1 != null) {
                            phase1.setMinSessionId(sessions.get(0).getId());
                        }

                        if(phase2a != null) {
                            // Mobile On, Amphiro off
                            if(phase1 == null) {
                                phase2a.setMinSessionId(sessions.get(0).getId());
                            } else {
                                long[] indexes = sessions.interpolate(phase2a.getStartTimestamp(),
                                                                      phase1.getDays(),
                                                                      phase2a.getDays(),
                                                                      survey.getHouseholdMemberTotal(),
                                                                      survey.getShowersPerWeek(),
                                                                      devices.size());
                                if (indexes == null) {
                                    throw new RuntimeException(String.format("Failed to interpolate session indexes for phase [%s]. Total sessions [%d]",
                                                                             phase2a.getPhase(),
                                                                             sessions.size()));
                                }

                                phase1.setMaxSessionId(indexes[0]);
                                phase2a.setMinSessionId(indexes[1]);
                            }

                            if(phase3 == null) {
                                phase2a.setMaxSessionId(sessions.get(sessions.size() - 1).getId());
                            } else {
                                AmphiroSession phase3NearestSession = sessions.getNearestSession(phase3.getStartTimestamp());

                                if (phase3NearestSession == null) {
                                    throw new RuntimeException(String.format("Failed to find the session index associated with phase [%s]. Total sessions [%d]",
                                                                             phase3.getPhase(),
                                                                             sessions.size()));
                                }

                                phase2a.setMaxSessionId(sessions.getPreviousId(phase3NearestSession.getId()));

                                phase3.setMinSessionId(phase3NearestSession.getId());
                                phase3.setMaxSessionId(sessions.get(sessions.size() - 1).getId());
                            }
                        } else if(phase2b != null) {
                            // Mobile Off, Amphiro On
                            if(phase1 == null) {
                                phase2b.setMinSessionId(sessions.get(0).getId());
                            } else {
                                AmphiroSession phase2NearestSession = sessions.getNearestSession(phase2b.getStartTimestamp());

                                if (phase2NearestSession == null) {
                                    throw new RuntimeException(String.format("Failed to find the session index associated with phase [%s]. Total sessions [%d]",
                                                                             phase2b.getPhase(),
                                                                             sessions.size()));
                                }

                                phase1.setMaxSessionId(sessions.getPreviousId(phase2NearestSession.getId()));
                                phase2b.setMinSessionId(phase2NearestSession.getId());
                            }

                            if(phase3 == null) {
                                phase2b.setMaxSessionId(sessions.get(sessions.size() - 1).getId());
                            } else {
                                long[] indexes = sessions.interpolate(phase3.getStartTimestamp(),
                                                                      phase2b.getDays(),
                                                                      phase3.getDays(),
                                                                      survey.getHouseholdMemberTotal(),
                                                                      survey.getShowersPerWeek(),
                                                                      devices.size());
                                if (indexes == null) {
                                    throw new RuntimeException(String.format("Failed to interpolate session indexes for phase [%s]. Total sessions [%d]",
                                                                             phase3.getPhase(),
                                                                             sessions.size()));
                                }

                                phase2b.setMaxSessionId(indexes[0]);
                                phase3.setMinSessionId(indexes[1]);

                                phase3.setMaxSessionId(sessions.get(sessions.size() - 1).getId());
                            }
                        } else {
                            // Only the baseline phase may exist
                            if (phase1 != null) {
                                phase1.setMaxSessionId(sessions.get(sessions.size() - 1).getId());
                            }
                        }

                        // Check intervals
                        if (phase2a != null) {
                            // Mobile On, Amphiro Off: The phase 3 left limit is considered the most accurate.
                            if (phase3 != null) {
                                if (phase3.getMinSessionId() > phase3.getMaxSessionId()) {
                                    phase3.setMaxSessionId(phase3.getMinSessionId());
                                }
                                // Align phase 2a
                                phase2a.setMaxSessionId(sessions.getPreviousId(phase3.getMinSessionId()));
                            }
                            if(phase2a.getMaxSessionId() < phase2a.getMinSessionId()) {
                                phase2a.setMinSessionId(phase2a.getMaxSessionId());
                            }
                            // Align phase 1
                            if (phase1 != null) {
                                phase1.setMaxSessionId(sessions.getPreviousId(phase2a.getMinSessionId()));
                                if (phase1.getMaxSessionId() == null) {
                                    phase1.setMinSessionId(null);
                                }
                            }
                        } else {
                            // Mobile Off, Amphiro On: The phase 2b left limit is considered the most accurate.
                            if (phase2b.getMinSessionId() > phase2b.getMaxSessionId()) {
                                phase2b.setMaxSessionId(phase2b.getMinSessionId());
                            }
                            // Align phase 3b
                            if (phase3 != null) {
                                phase3.setMinSessionId(sessions.getNextId(phase2b.getMaxSessionId()));
                                if (phase3.getMinSessionId() == null) {
                                    phase3.setMaxSessionId(null);
                                }
                            }
                            // Align phase 1
                            if (phase1 != null) {
                                phase1.setMaxSessionId(sessions.getPreviousId(phase2b.getMinSessionId()));
                                if (phase1.getMaxSessionId() == null) {
                                    phase1.setMinSessionId(null);
                                }
                            }
                        }

                        phaseTimeline.validate();

                        row = new ArrayList<String>();

                        row.add(user.getKey().toString());
                        row.add(user.getUsername());
                        row.add(device.getDeviceKey().toString());
                        row.add(device.getName());

                        createPhaseRowWithShowers(EnumPhase.BASELINE,row, phase1, formatter);
                        if(phase2a != null) {
                            createPhaseRowWithShowers(phase2a.getPhase(), row, phase2a, formatter);
                        } else {
                            createPhaseRowWithShowers(phase2b.getPhase(),row, phase2b, formatter);
                        }
                        createPhaseRowWithShowers(EnumPhase.MOBILE_ON_AMPHIRO_ON,row, phase3, formatter);

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
                if (s1.getVolume() <= s2.getVolume()) {
                    return -1;
                } else {
                    return 1;
                }
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

    /**
     * Helper class for managing amphiro b1 sessions.
     */
    private static class SessionCollection {

        List<AmphiroAbstractSession> sessions;

        public SessionCollection(List<AmphiroAbstractSession> sessions) {
            this.sessions = sessions;

            Collections.sort(sessions, new Comparator<AmphiroAbstractSession>() {

                @Override
                public int compare(AmphiroAbstractSession s1, AmphiroAbstractSession s2) {
                    AmphiroSession as1 = (AmphiroSession) s1;
                    AmphiroSession as2 = (AmphiroSession) s2;

                    if (as1.getId() == as2.getId()) {
                        throw new RuntimeException(String.format("Found duplicated session index [%d]",  as1.getId()));
                    } else if (as1.getId() < as2.getId()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
        }

        /**
         * Returns the element at the specified position in this list.
         *
         * @param index index of the element to return.
         * @return the element at the specified position in this list.
         */
        public AmphiroSession get(int index) {
            return (AmphiroSession) sessions.get(index);
        }

        /**
         * Returns the number of elements in this list.
         *
         * @return the number of elements in this list.
         */
        public int size() {
            return sessions.size();
        }

        /**
         * Returns true if this list contains no elements.
         *
         * @return true if this list contains no elements.
         */
        public boolean isEmpty() {
            return sessions.isEmpty();
        }

        /**
         * Removes all the historical data before the initial device pairing.
         *
         * @throws RuntimeException if a duplicate session index is found.
         */
        public void cleanPairingSessions() throws RuntimeException {

            int count = 0;

            for(AmphiroAbstractSession session : sessions) {
                AmphiroSession amphiroSession = (AmphiroSession) session;

                if(amphiroSession.isHistory()) {
                    count++;
                } else {
                    break;
                }
            }

            for (int i = 0; i < count; i++) {
                // Do not remove sessions
                //sessions.remove(0);
            }
        }

        /**
         * Given a timestamp, it returns the most recent real-time session before it.
         *
         * @param timestamp the timestamp to search.
         * @return the amphiro b1 real-time session that occurred before the given timestamp.
         */
        public AmphiroSession getRealTimeSessionBefore(long timestamp) {
            AmphiroSession result = null;

            for(AmphiroAbstractSession session : sessions) {
                AmphiroSession amphiroSession = (AmphiroSession) session;

                if ((!amphiroSession.isHistory()) && (amphiroSession.getTimestamp() <= timestamp)) {
                    if ((result == null) || (result.getTimestamp() < amphiroSession.getTimestamp())) {
                        result = amphiroSession;
                    }
                }
            }

            return result;
        }

        /**
         * Given a timestamp, it returns the most recent real-time session after it.
         *
         * @param timestamp the timestamp to search.
         * @return the amphiro b1 real-time session that occurred after the given timestamp.
         */
        public AmphiroSession getRealTimeSessionAfter(long timestamp) {
            AmphiroSession result = null;

            for(AmphiroAbstractSession session : sessions) {
                AmphiroSession amphiroSession = (AmphiroSession) session;

                if ((!amphiroSession.isHistory()) && (amphiroSession.getTimestamp() >= timestamp)) {
                    if ((result == null) || (result.getTimestamp() > amphiroSession.getTimestamp())) {
                        result = amphiroSession;
                    }
                }
            }

            return result;
        }


        /**
         * Given a timestamp, it returns the most recent real-time session before or after it.
         *
         * @param timestamp the timestamp to search.
         * @return the amphiro b1 real-time session that occurred most recently before or after the given timestamp.
         */
        public AmphiroSession getNearestSession(long timestamp) {
            AmphiroSession result = null;

            for(AmphiroAbstractSession session : sessions) {
                AmphiroSession amphiroSession = (AmphiroSession) session;

                if (!amphiroSession.isHistory()) {
                    if ((result == null) ||
                        (Math.abs(result.getTimestamp() - timestamp) >
                         Math.abs(amphiroSession.getTimestamp() - timestamp))) {
                        result = amphiroSession;
                    }
                }
            }

            return result;
        }

        /**
         * Returns the previous session id for the given id.
         *
         * @param id the session id to search for.
         * @return the id of the session which is immediately before the session with the given id.
         */
        public Long getPreviousId(long id) {
            for (int i = 0, count = sessions.size() - 1; i < count; i++) {
                if (((AmphiroSession) sessions.get(i + 1)).getId() == id) {
                    return ((AmphiroSession) sessions.get(i)).getId();
                }
            }

            throw new RuntimeException(String.format("Cannot find previous id for shower id [%d]", id));
        }

        /**
         * Returns the next session id for the given id.
         *
         * @param id the session id to search for.
         * @return the id of the session which is immediately after the session with the given id.
         */
        public Long getNextId(long id) {
            for (int i = 0, count = sessions.size() - 1; i < count; i++) {
                if (((AmphiroSession) sessions.get(i)).getId() == id) {
                    return ((AmphiroSession) sessions.get(i + 1)).getId();
                }
            }

            throw new RuntimeException(String.format("Cannot find next id for shower id [%d]", id));
        }

        /**
         * Given a timestamp, the two most recent real time sessions before and
         * after it are computed. The session index interval defined by the two
         * sessions is separated into two sets based on either (a) the given
         * weight values or (b) the number of household members and the number
         * of showers per week in the specific household. In the latter case,
         * the number of amphiro b1 devices is also considered.
         *
         * @param timestamp reference timestamp
         * @param interval1 weight for the first set.
         * @param interval2 weight of the second set.
         * @param householdMembers the number of household members.
         * @param showerPerWeek the number of showers per week.
         * @param numberOfAmphiro the number of installed amphiro devices.
         * @return the indexes of sessions that split the sessions between the
         *         given indexes in two sets based on the given weights.
         */
        public long[] interpolate(long timestamp, float interval1, float interval2, int householdMembers, Integer showerPerWeek, int numberOfAmphiro) {
            if(numberOfAmphiro > 3) {
                numberOfAmphiro = 3;
            }
            if (showerPerWeek == null) {
                showerPerWeek = 0;
            }

            AmphiroSession sessionBefore = getRealTimeSessionBefore(timestamp);
            AmphiroSession sessionAfter = getRealTimeSessionAfter(timestamp);

            if ((sessionBefore == null) || (sessionAfter == null)) {
                return null;
            }

            long[] result = new long[] { sessionBefore.getId(), sessionAfter.getId()};

            int count = 0;

            // Count sessions between the two indexes
            for(AmphiroAbstractSession session : sessions) {
                AmphiroSession amphiroSession = (AmphiroSession) session;
                if ((amphiroSession.getId() > sessionBefore.getId()) && (amphiroSession.getId() < sessionAfter.getId())) {
                    count++;
                }
            }

            if (count == 0) {
                // No sessions found between the two indexes.
                return result;
            }

            // Check the result can be computed based on the household members
            // and number of showers per week
            int middle = 0;

            float threshold = ((float) showerPerWeek / 7) * 20 / numberOfAmphiro;
            if (count <= threshold) {
                middle = Math.round((count * (timestamp - sessionBefore.getTimestamp())) /
                                    (sessionAfter.getTimestamp() - sessionBefore.getTimestamp()));
            } else {
                // Compute the middle index based on the weight values.
                middle = Math.round(count * interval1 / (interval1 + interval2));
            }

            count = 0;

            for (AmphiroAbstractSession session : sessions) {
                AmphiroSession amphiroSession = (AmphiroSession) session;
                if ((amphiroSession.getId() > sessionBefore.getId()) && (amphiroSession.getId() < sessionAfter.getId())) {
                    if (count < middle) {
                        count++;
                        result[0] = amphiroSession.getId();
                    } else {
                        result[1] = amphiroSession.getId();
                        return result;
                    }
                }
            }

            return result;
        }
    }
}
