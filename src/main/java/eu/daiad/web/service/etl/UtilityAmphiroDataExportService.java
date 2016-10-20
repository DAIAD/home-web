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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.domain.application.SurveyEntity;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceAmphiroConfiguration;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.ProfileHistoryEntry;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IProfileRepository;
import eu.daiad.web.repository.application.IUserRepository;

/**
 * Service that exports amphiro b1 data for a utility.
 */
@Service
public class UtilityAmphiroDataExportService extends AbstractUtilityDataExportService {

    /**
     * Amphiro b1 OFF configuration title.
     */
    private static final String AMPHIRO_OFF_CONFIGURATION = "Off Configuration";

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing user profile data.
     */
    @Autowired
    private IProfileRepository profileRepository;

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

            // Export sessions
            exportAmphiroSessionData(query, result);
            
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
        long totalRows = 0;

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

                totalRows++;

                printer.printRecord(row);
            }
        }
        
        printer.flush();
        printer.close();
        
        result.increment(totalRows);

        result.getFiles().add(new FileLabelPair(new File(filename), "user.csv"));
    }


    /**
     * Exports phase start/end timestamp for amphiro b1.
     * 
     * @param query the query that selects the data to export.
     * @param result export result.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportPhaseTimestamps(UtilityDataExportQuery query, ExportResult result) throws IOException {
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
                for (Device d : deviceRepository.getUserDevices(user.getKey(), new DeviceRegistrationQuery())) {
                    if (d.getType() == EnumDeviceType.AMPHIRO) {
                        try {
                            PhaseTimeline phaseTimeline = constructPhaseTimeline(user.getKey(), d.getKey());

                            row = new ArrayList<String>();

                            row.add(user.getKey().toString());
                            row.add(user.getUsername());
                            row.add(d.getKey().toString());
                            row.add(((AmphiroDevice) d).getName());

                            this.createPhaseRowWithTimestamps(EnumPhase.BASELINE, row, phaseTimeline, formatter);
                            if(phaseTimeline.getPhase(EnumPhase.MOBILE_ON_AMPHIRO_OFF) != null) {
                                this.createPhaseRowWithTimestamps(EnumPhase.MOBILE_ON_AMPHIRO_OFF, row, phaseTimeline, formatter);
                            } else if(phaseTimeline.getPhase(EnumPhase.MOBILE_OFF_AMPHIRO_ON) != null) {
                                this.createPhaseRowWithTimestamps(EnumPhase.MOBILE_OFF_AMPHIRO_ON, row, phaseTimeline, formatter);    
                            } else {
                                row.add("");
                                row.add("");
                                row.add("");
                            }
                            if(phaseTimeline.getPhase(EnumPhase.MOBILE_ON_AMPHIRO_ON) != null) {
                                this.createPhaseRowWithTimestamps(EnumPhase.MOBILE_ON_AMPHIRO_ON, row, phaseTimeline, formatter);
                            } else {
                                row.add("");
                                row.add("");
                                row.add("");
                            }

                            result.increment();
                            
                            printer.printRecord(row);
                        } catch(Exception ex) {
                            result.addMessage(user.getKey(),
                                              user.getUsername(),
                                              d.getKey(),
                                              String.format("Failed to export phase timestamp timeline for user [%s]: %s", 
                                                            user.getUsername(),
                                                            ex.getMessage()));
                        }
                    }
                }
            }
        }
        
        printer.flush();
        printer.close();
        
        result.getFiles().add(new FileLabelPair(new File(filename), "phase-timestamp.csv"));
    }

    /**
     * Export a single phase to a row.
     * 
     * @param type type of the phase to export.
     * @param row the row to append phase data.
     * @param phaseTimeline the phase timeline.
     * @param formatter formatter for date/time properties.
     */
    private void createPhaseRowWithTimestamps(EnumPhase type, List<String> row, PhaseTimeline phaseTimeline, DateTimeFormatter formatter) {
        Phase phase = phaseTimeline.getPhase(type);

        if(phase == null) {
            row.add(type.toString());
            row.add("");
            row.add("");                                
        } else {
            row.add(phase.getPhase().toString());
            row.add(new DateTime(phase.getStartTimestamp(), DateTimeZone.UTC).toString(formatter));
            row.add(new DateTime(phase.getEndTimestamp(), DateTimeZone.UTC).toString(formatter));                                
        }
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

        String dataFilename = createTemporaryFilename(query.getWorkingDirectory());
        String removeFilename = createTemporaryFilename(query.getWorkingDirectory());
        String filterFilename = createTemporaryFilename(query.getWorkingDirectory());

        DateTimeFormatter formatter = DateTimeFormat.forPattern(query.getDateFormat()).withZone(DateTimeZone.forID(query.getTimezone()));

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter dataPrinter = new CSVPrinter(
                                     new BufferedWriter(
                                         new OutputStreamWriter(
                                             new FileOutputStream(dataFilename, true),
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

        dataPrinter.printRecord(row);

        row = new ArrayList<String>();

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
                                .searchSessions(deviceNames.toArray(new String[] {}), DateTimeZone.forID(query.getTimezone()), sessionQuery);

                // Process shower sessions for every device
                for (AmphiroSessionCollection device : amphiroCollection.getDevices()) {
                    int total = device.getSessions().size();

                    List<AmphiroAbstractSession> sessions = device.getSessions();
                    List<AmphiroSession> removedSessions = new ArrayList<AmphiroSession>();
                    
                    // Remove sessions based on volume, duration and flow
                    for (int i = sessions.size() - 1; i >= 0; i--) {
                        AmphiroSession session = (AmphiroSession) sessions.get(i);
                        
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
                            totalRows++;
                            dataPrinter.printRecord(row);
                        }
                    }

                    // Export removed session
                    for(AmphiroSession session : removedSessions) {
                        row = createAmphiroSessionRow(user.getKey(), device.getDeviceKey(), formatter, session);

                        totalRows++;
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

                        removedIndexPrinter.printRecord(row);
                    }
                    
                    // Check if more than 30% of the sessions have been removed
                    if (removedSessions.size() > 0) {
                        if (((float) removedSessions.size() / total) > 0.3) {
                            result.addMessage(user.getKey(),
                                              user.getUsername(),
                                              device.getDeviceKey(),
                                              String.format("More than 30%% of shower sessions has been removed. Total [%d]. Removed [%d]", 
                                                            total,
                                                            removedSessions.size()));
                        }
                    }
                }
            }
        }
        
        dataPrinter.flush();
        dataPrinter.close();

        removedDataPrinter.flush();
        removedDataPrinter.close();

        removedIndexPrinter.flush();
        removedIndexPrinter.close();
        
        result.increment(totalRows);
        result.getFiles().add(new FileLabelPair(new File(dataFilename), "shower-data.csv"));
        result.getFiles().add(new FileLabelPair(new File(removeFilename), "shower-data-removed.csv"));
        result.getFiles().add(new FileLabelPair(new File(filterFilename), "shower-data-removed-index.csv"));
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
        
        result.getFiles().add(new FileLabelPair(new File(filename), "error.csv"));
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
                                .searchSessions(deviceNames.toArray(new String[] {}), DateTimeZone.forID(query.getTimezone()), sessionQuery);
                
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
                        PhaseTimeline phaseTimeline = this.constructPhaseTimeline(user.getKey(), device.getDeviceKey());
                        
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
    
                        this.createPhaseRowWithShowers(EnumPhase.BASELINE,row, phase1, formatter);
                        if(phase2a != null) {
                            this.createPhaseRowWithShowers(phase2a.getPhase(), row, phase2a, formatter);
                        } else {
                            this.createPhaseRowWithShowers(phase2b.getPhase(),row, phase2b, formatter);    
                        }
                        this.createPhaseRowWithShowers(EnumPhase.MOBILE_ON_AMPHIRO_ON,row, phase3, formatter);
    
                        result.increment();
                        
                        printer.printRecord(row);
                    } catch(Exception ex) {
                        result.addMessage(user.getKey(),
                                          user.getUsername(),
                                          device.getDeviceKey(),
                                          String.format("Failed to export phase sid timeline for user [%s]: %s", 
                                                        user.getUsername(),
                                                        ex.getMessage()));
                    }
                }
            }
        }
        
        printer.flush();
        printer.close();
        
        result.getFiles().add(new FileLabelPair(new File(filename), "phase-shower-id.csv"));
    }

    /**
     * Constructs a phase timeline for a user and amphiro b1 device.
     * 
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @return a valid phase timeline.
     * @throws RuntimeException if invalid timeline is detected e.g. phases overlap.
     */
    private PhaseTimeline constructPhaseTimeline(UUID userKey, UUID deviceKey) throws RuntimeException {
        // Get all transitions
        TransitionTimeline transitionTimeline = new TransitionTimeline();
        
        for (ProfileHistoryEntry entry : profileRepository.getProfileHistoryByUserKey(userKey)) {
            switch (entry.getMobileMode()) {
                case ACTIVE:
                    // Since profile update acknowledgement feature may have not
                    // been implemented when the profile has been updated, we
                    // use the profile update timestamp.
                    //
                    // Also, if the acknowledged enabled timestamp is prior to
                    // the update timestamp, we use the latter.
                    if((entry.getEnabledOn() == null) || (entry.getUpdatedOn().getMillis() > entry.getEnabledOn().getMillis())) {
                        transitionTimeline.add(EnumTransition.MOBILE_ON, entry.getUpdatedOn().getMillis());
                    } else {
                        transitionTimeline.add(EnumTransition.MOBILE_ON, entry.getEnabledOn().getMillis());
                    }
                    break;
                default:
                    // Ignore all transitions except for activation.
            }
        }

        for(DeviceAmphiroConfiguration entry : deviceRepository.getDeviceConfigurationHistory(deviceKey)) {
            switch(entry.getTitle()) {
                case AMPHIRO_OFF_CONFIGURATION:
                    // Since amphiro b1 configuration update acknowledgement
                    // feature may have not been implemented when the device was
                    // paired, we use the device registration date.
                    transitionTimeline.add(EnumTransition.AMHIRO_PAIRED, entry.getCreatedOn());
                    break;
                default:
                    // If acknowledgement is not available, use the creation timestamp.
                    if((entry.getEnabledOn() == null) || (entry.getCreatedOn() > entry.getEnabledOn())) {
                        transitionTimeline.add(EnumTransition.AMHIRO_ON, entry.getCreatedOn());
                    } else {
                        transitionTimeline.add(EnumTransition.AMHIRO_ON, entry.getEnabledOn());
                    }
            }
        }

        // Once constructed the timeline, derive phases.
        PhaseTimeline phaseTimeline = new PhaseTimeline();

        Long timestampAmphiroPaired = transitionTimeline.getTimestampByType(EnumTransition.AMHIRO_PAIRED);
        Long timestampAmphiroOn = transitionTimeline.getTimestampByType(EnumTransition.AMHIRO_ON);
        Long timestampMobileOn = transitionTimeline.getTimestampByType(EnumTransition.MOBILE_ON);
        
        if (timestampAmphiroPaired == null) {
            // Could not resolve phases
            throw new RuntimeException("Transition [AMHIRO_OFF] could not be found.");
        } else if ((timestampAmphiroOn == null) && (timestampMobileOn == null)) {
            // Still in learning mode and both mobile/amphiro b1 are disabled
            phaseTimeline.add(EnumPhase.BASELINE, timestampAmphiroPaired, DateTime.now().getMillis());
        } else if (timestampAmphiroOn == null) {
            // Only mobile has been enabled
            if(timestampAmphiroPaired < timestampMobileOn) {
                phaseTimeline.add(EnumPhase.BASELINE, timestampAmphiroPaired, timestampMobileOn);
                phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_OFF, timestampMobileOn, DateTime.now().getMillis());
            } else {
                phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_OFF, timestampAmphiroPaired, DateTime.now().getMillis());
            }
        } else if (timestampMobileOn == null) {
            // Only amphiro b1 has been enabled
            if(timestampAmphiroPaired < timestampAmphiroOn) {
                phaseTimeline.add(EnumPhase.BASELINE, timestampAmphiroPaired, timestampAmphiroOn);
                phaseTimeline.add(EnumPhase.MOBILE_OFF_AMPHIRO_ON, timestampAmphiroOn, DateTime.now().getMillis());
            } else {
                phaseTimeline.add(EnumPhase.MOBILE_OFF_AMPHIRO_ON, timestampAmphiroPaired, DateTime.now().getMillis());
            }
        } else {
            // Both amphiro b1 and mobile are on. Decide phase ordering
            if (timestampAmphiroOn < timestampMobileOn) {
                // Amphiro b1 enabled first
                if(timestampAmphiroPaired < timestampAmphiroOn) {
                    phaseTimeline.add(EnumPhase.BASELINE, timestampAmphiroPaired, timestampAmphiroOn);
                    phaseTimeline.add(EnumPhase.MOBILE_OFF_AMPHIRO_ON, timestampAmphiroOn, timestampMobileOn);
                    phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_ON, timestampMobileOn, DateTime.now().getMillis());
                } else {
                    phaseTimeline.add(EnumPhase.MOBILE_OFF_AMPHIRO_ON, timestampAmphiroPaired, timestampMobileOn);
                    phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_ON, timestampMobileOn, DateTime.now().getMillis());
                }
            } else {
                // Mobile enabled first
                if(timestampAmphiroPaired < timestampMobileOn) {
                    phaseTimeline.add(EnumPhase.BASELINE, timestampAmphiroPaired, timestampMobileOn);
                    phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_OFF, timestampMobileOn, timestampAmphiroOn);
                    phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_ON, timestampAmphiroOn, DateTime.now().getMillis());
                } else {
                    phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_OFF, timestampAmphiroPaired, timestampAmphiroOn);
                    phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_ON, timestampAmphiroOn, DateTime.now().getMillis());
                }
            }
        }
        
        phaseTimeline.validate();

        return phaseTimeline;
    }

    /**
     * For a list of shower sessions, find the volume threshold for the given
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
     * Possible state transitions e.g. configuration or profile update.
     */
    private static enum EnumTransition {
        /**
         * Initial amphiro b1 state. This is the default OFF configuration
         * assigned to a device when it is paired for the first time.
         */
        AMHIRO_PAIRED,
        /**
         * Amphiro b1 has been enabled.
         */
        AMHIRO_ON,
        /**
         * Mobile application has been enabled.
         */
        MOBILE_ON; 
    }

    /**
     * Represents a state transition.
     */
    private static class Transition {

        private EnumTransition transition;

        private long timestamp;

        public Transition(EnumTransition transition, long timestamp) {
            this.transition = transition;
            this.timestamp = timestamp;
        }

        public EnumTransition getTransition() {
            return transition;
        }

        public long getTimestamp() {
            return timestamp;
        }

    }

    /**
     * Represents the timeline of state transitions.
     */
    private static class TransitionTimeline {
        
        List<Transition> transitions = new ArrayList<Transition>();

        /**
         * Adds a new transition to the timeline.
         * 
         * @param transition the new transition type.
         * @param timestamp the timestamp of the transition.
         * @throws Exception if transition already exists or the timestamp is the same with that of an existing transition.
         */
        public void add(EnumTransition transition, long timestamp) throws RuntimeException {
            this.add(new Transition(transition, timestamp));
        }

        /**
         * Adds a new transition to the timeline.
         * 
         * @param transition the new transition.
         * @throws Exception if transition already exists or the timestamp is the same with that of an existing transition.
         */
        public void add(Transition transition) throws RuntimeException {
            transitions.add(transition);
            
            Collections.sort(transitions, new Comparator<Transition>() {

                @Override
                public int compare(Transition t1, Transition t2) {
                    if (t1.getTimestamp() == t2.getTimestamp()) {
                        throw new RuntimeException("Transition timestamp must be unique.");
                    } else if (t1.getTimestamp() < t2.getTimestamp()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
        }
        
        public Long getTimestampByType(EnumTransition type) {
            for(Transition t : transitions) {
                if(t.getTransition().equals(type)) {
                    return t.getTimestamp();
                }
            }
            
            return null;
        }
    }

    /**
     * Trial phases
     */
    private static enum EnumPhase {
        BASELINE, MOBILE_OFF_AMPHIRO_ON, MOBILE_ON_AMPHIRO_OFF, MOBILE_ON_AMPHIRO_ON, SOCIAL_ON;
    }

    /**
     * Represents a phase in trial.
     */
    private static class Phase {

        private EnumPhase phase;

        private long startTimestamp;

        private long endTimestamp;

        private Long minSessionId;
        
        private Long maxSessionId;

        public Phase(EnumPhase phase, long startTimestamp, long endTimestamp) {
            this.phase = phase;
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
        }

        public EnumPhase getPhase() {
            return phase;
        }

        public long getStartTimestamp() {
            return startTimestamp;
        }

        public long getEndTimestamp() {
            return endTimestamp;
        }

        public Long getMinSessionId() {
            return minSessionId;
        }

        public void setMinSessionId(Long minSessionId) {
            this.minSessionId = minSessionId;
        }

        public Long getMaxSessionId() {
            return maxSessionId;
        }

        public void setMaxSessionId(Long maxSessionId) {
            this.maxSessionId = maxSessionId;
        }
        
        public int getDays() {
            return Days.daysBetween(new DateTime(startTimestamp, DateTimeZone.UTC),
                                    new DateTime(endTimestamp, DateTimeZone.UTC)).getDays();
        }

    }

    
    /**
     * Represents the timeline of trial phases.
     */
    private static class PhaseTimeline {
        
        List<Phase> phases = new ArrayList<Phase>();

        public Phase getPhase(EnumPhase phase) {
            for(Phase p : phases) {
                if(p.getPhase().equals(phase)) {
                    return p;
                }
            }
            
            return null;
        }

        /**
         * Adds a new phase to the timeline.
         * 
         * @param phase phase type.
         * @param startTimestamp start timestamp.
         * @param endTimestamp end timestamp.
         * @throws RuntimeException if phase already exists or the timestamp is the same with that of an existing phase. 
         */
        public void add(EnumPhase phase, long startTimestamp, long endTimestamp) throws RuntimeException {
            this.add(new Phase(phase, startTimestamp, endTimestamp));
        }

        /**
         * Adds a new phase to the timeline.
         * 
         * @param phase the new phase.
         * @throws RuntimeException if phase already exists or the timestamp is the same with that of an existing phase.
         */
        public void add(Phase phase) throws RuntimeException {
            for(Phase p : phases) {
                if(p.getPhase().equals(phase.getPhase())) {
                    throw new RuntimeException(String.format("Phase [%s] is not unique in timeline.", phase.getPhase().toString()));
                }
            }
            
            phases.add(phase);
            
            Collections.sort(phases, new Comparator<Phase>() {

                @Override
                public int compare(Phase p1, Phase p2) {
                    if (p1.getStartTimestamp() <= p2.getStartTimestamp()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
        }
        
        /**
         * Validates phases
         */
        public void validate() {
            for (Phase p1 : phases) {
                for (Phase p2 : phases) {
                    if (!p1.getPhase().equals(p2.getPhase())) {
                        if((p1.getEndTimestamp() > p2.getStartTimestamp()) && (p1.getEndTimestamp() < p2.getEndTimestamp())) {
                            throw new RuntimeException(String.format("Phases [%s] and [%s] timestamp overlap.", p1.getPhase(), p2.getPhase()));
                        }
                        if((p1.getStartTimestamp() > p2.getStartTimestamp()) && (p1.getStartTimestamp() < p2.getEndTimestamp())) {
                            throw new RuntimeException(String.format("Phases [%s] and [%s] timestamp overlap.", p1.getPhase(), p2.getPhase()));
                        }
                    }
                    
                    if ((p1 != p2) && 
                        (p1.getMinSessionId() != null) && (p1.getMaxSessionId() != null) &&
                        (p2.getMinSessionId() != null) && (p2.getMaxSessionId() != null)) {
                        if((p1.getMaxSessionId() >= p2.getMinSessionId()) && (p1.getMaxSessionId() <= p2.getMaxSessionId())) {
                            throw new RuntimeException(String.format("Phases [%s] and [%s] sid overlap. %s", p1.getPhase(), p2.getPhase(), this).trim());
                        }
                        if((p1.getMinSessionId() >= p2.getMinSessionId()) && (p1.getMinSessionId() <= p2.getMaxSessionId())) {
                            throw new RuntimeException(String.format("Phases [%s] and [%s] sid overlap. %s", p1.getPhase(), p2.getPhase(), this).trim());
                        }
                    }
                }
            }
            for (Phase p : phases) {
                if(p.getStartTimestamp() > p.getEndTimestamp()) {
                    throw new RuntimeException(String.format("Invalid interval for phase [%s].", p.getPhase()));
                }
                if ((p.getMinSessionId() == null) && (p.getMaxSessionId() == null)) {
                    // Ignore
                } else if ((p.getMinSessionId() != null) && (p.getMaxSessionId() != null)) {
                    if(p.getMinSessionId() > p.getMaxSessionId()) {
                        throw new RuntimeException(String.format("Invalid shower id interval [%d, %d] for phase [%s]. %s", 
                                                                 p.getMinSessionId(),
                                                                 p.getMaxSessionId(),
                                                                 p.getPhase(),
                                                                 this).trim());
                    }
                } else {
                    throw new RuntimeException(String.format("Failed to derive phase [%s] min/max shower id. %s", p.getPhase(), this));
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder text = new StringBuilder();

            for (Phase p : phases) {
                text.append(String.format("%s [%d - %d] ", p.getPhase(), p.getMinSessionId(), p.getMaxSessionId()));
            }
            return text.toString();
        }
        
        
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
            
            return null;
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
            
            return null;
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
