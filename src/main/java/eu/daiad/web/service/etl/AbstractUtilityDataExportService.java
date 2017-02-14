package eu.daiad.web.service.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;

import eu.daiad.web.domain.application.SurveyEntity;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceAmphiroConfiguration;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.profile.ProfileHistoryEntry;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IProfileRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.etl.model.EnumPhase;
import eu.daiad.web.service.etl.model.EnumTransition;
import eu.daiad.web.service.etl.model.Phase;
import eu.daiad.web.service.etl.model.PhaseTimeline;
import eu.daiad.web.service.etl.model.Transition;
import eu.daiad.web.service.etl.model.TransitionTimeline;

/**
 * Helper abstract class that provides utility methods to services that export
 * utility data.
 */
public abstract class AbstractUtilityDataExportService extends AbstractDataExportService {

    /**
     * Amphiro b1 OFF configuration title.
     */
    private static final String AMPHIRO_OFF_CONFIGURATION = "Off Configuration";

    /**
     * Repository for accessing user data.
     */
    @Autowired
    protected IUserRepository userRepository;

    /**
     * Repository for accessing user profile data.
     */
    @Autowired
    private IProfileRepository profileRepository;

    /**
     * Repository for accessing device (smart water meter or amphiro b1) data.
     */
    @Autowired
    protected IDeviceRepository deviceRepository;

    /**
     * Creates a filename for the utility exported data file.
     *
     * @param targetDirectory target directory
     * @param filenamePrefix filename prefix.
     * @param extension filename extension.
     * @return the filename.
     */
    protected String createUtilityExportFilename(String targetDirectory, String filenamePrefix, String extension) {
        String filename;

        if(StringUtils.isBlank(filenamePrefix)) {
            filenamePrefix = "export";
        } else {
            filenamePrefix = filenamePrefix.replaceAll("[^a-zA-Z0-9]", "-");
        }

        DateTimeFormatter fileDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        if(StringUtils.isBlank(extension)) {
            filename = String.format("%s-%s", filenamePrefix.toLowerCase(), new DateTime().toString(fileDateFormatter));
        } else {
            filename = String.format("%s-%s.%s", filenamePrefix.toLowerCase(), new DateTime().toString(fileDateFormatter), extension);
        }


        return FilenameUtils.concat(targetDirectory, filename);
    }

    /**
     * Exports phase start/end timestamps.
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @throws IOException if file creation fails.
     */
    protected void exportPhaseTimestamps(UtilityDataExportQuery query, ExportResult result) throws IOException {
        if (query.getSource() == EnumDataSource.NONE) {
            return;
        }

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
        switch(query.getSource()) {
            case METER:
                row.add("meter key");
                row.add("meter serial");
                break;
            case AMPHIRO:
                row.add("device key");
                row.add("device name");
                break;
            case NONE:
                // Ignore
                break;
        }

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

        printer.printRecord(row);

        long totalRows = 0;
        switch(query.getSource()) {
            case METER:
                totalRows = exportMeterPhaseTimestamps(query, result, formatter, printer);
                break;
            case AMPHIRO:
                totalRows = exportAmphiroPhaseTimestamps(query, result, formatter, printer);
                break;
            case NONE:
                // Ignore
                break;
        }

        printer.flush();
        printer.close();

        result.increment(totalRows);
        result.getFiles().add(new FileLabelPair(new File(filename), "phase-timestamp.csv", totalRows));
    }

    /**
     * Exports phase start/end timestamp for amphiro b1.
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @param formatter date formatter.
     * @param printer CSV file printer.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private long exportAmphiroPhaseTimestamps(UtilityDataExportQuery query, ExportResult result, DateTimeFormatter formatter, CSVPrinter printer) throws IOException {
        long totalRows = 0;

        for (SurveyEntity survey : userRepository.getSurveyDataByUtilityId(query.getUtility().getId())) {
            AuthenticatedUser user = userRepository.getUserByName(survey.getUsername());

            if (user != null) {
                for (Device device : deviceRepository.getUserDevices(user.getKey(), new DeviceRegistrationQuery())) {
                    if (device.getType() == EnumDeviceType.AMPHIRO) {
                        try {
                            printPhaseTimeline(user, device, formatter, printer);
                            totalRows++;
                        } catch(Exception ex) {
                            result.addMessage(user.getKey(),
                                              user.getUsername(),
                                              device.getKey(),
                                              String.format("Failed to export phase timestamp timeline for user [%s]: %s",
                                                            user.getUsername(),
                                                            ex.getMessage()));
                        }
                    }
                }
            }
        }

        return totalRows;
    }

    /**
     * Exports phase start/end timestamp for smart water meters.
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @param formatter date formatter.
     * @param printer CSV file printer.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private long exportMeterPhaseTimestamps(UtilityDataExportQuery query, ExportResult result, DateTimeFormatter formatter, CSVPrinter printer) throws IOException {
        long totalRows = 0;

        for (SurveyEntity survey : userRepository.getSurveyDataByUtilityId(query.getUtility().getId())) {
            AuthenticatedUser user = userRepository.getUserByName(survey.getUsername());

            if (user != null) {
                for (Device device : deviceRepository.getUserDevices(user.getKey(), new DeviceRegistrationQuery())) {
                    if(device.getType() == EnumDeviceType.METER) {
                        try {
                            printPhaseTimeline(user, device, formatter, printer);
                            totalRows++;
                        } catch(Exception ex) {
                            result.addMessage(user.getKey(),
                                              user.getUsername(),
                                              device.getKey(),
                                              String.format("Failed to export phase timestamp timeline for user [%s]: %s",
                                                            user.getUsername(),
                                                            ex.getMessage()));
                        }
                    }
                }
            }
        }

        return totalRows;
    }

    private void printPhaseTimeline(AuthenticatedUser user, Device device, DateTimeFormatter formatter, CSVPrinter printer) throws IOException {
        PhaseTimeline timeline;
        List<String> row = new ArrayList<String>();

        if (device.getType() == EnumDeviceType.AMPHIRO) {
            timeline = constructAmphiroPhaseTimeline(user.getKey(), device.getKey());
        } else {
            timeline = constructMeterPhaseTimeline(user.getKey());
        }

        row.add(user.getKey().toString());
        row.add(user.getUsername());
        row.add(device.getKey().toString());
        if (device.getType() == EnumDeviceType.AMPHIRO) {
            row.add(((AmphiroDevice) device).getName());
        } else {
            row.add(((WaterMeterDevice) device).getSerial());
        }

        if(timeline.size() > 4) {
            throw new RuntimeException(String.format("Invalid timeline size [%d].", timeline.size()));
        }

        for (int i = 0, size = timeline.size(); i < size; i++) {
            Phase phase = timeline.get(i);

            if (phase.getPhase() == EnumPhase.EMPTY) {
                if (i == 0) {
                    row.add("BASELINE");
                } else {
                    row.add("");
                }
                row.add("");
                row.add("");
            } else {
                createPhaseRowWithTimestamps(phase, row, formatter);
            }
        }
        // Add empty entries
        for (int i = timeline.size(); i < 4; i++) {
            row.add("");
            row.add("");
            row.add("");
        }

        printer.printRecord(row);
    }

    /**
     * Export a single phase to a row.
     *
     * @param phase phase to export.
     * @param row the row to append phase data.
     * @param phaseTimeline the phase timeline.
     * @param formatter formatter for date/time properties.
     */
    private void createPhaseRowWithTimestamps(Phase phase, List<String> row, DateTimeFormatter formatter) {
        row.add(phase.getPhase().merge().toString());
        row.add(new DateTime(phase.getStartTimestamp(), DateTimeZone.UTC).toString(formatter));
        row.add(new DateTime(phase.getEndTimestamp(), DateTimeZone.UTC).toString(formatter));
    }

    /**
     * Updates a transition timeline using profile information.
     *
     * @param transitionTimeline the timeline to update.
     * @param userKey the key of the user whose profile is used to extract event timestamps.
     */
    private void updateTimelineWithProfile(TransitionTimeline transitionTimeline, UUID userKey) {
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
            if(entry.isSocialEnabled()) {
                if((entry.getEnabledOn() == null) || (entry.getUpdatedOn().getMillis() > entry.getEnabledOn().getMillis())) {
                    transitionTimeline.add(EnumTransition.SOCIAL_ON, entry.getUpdatedOn().getMillis());
                } else {
                    transitionTimeline.add(EnumTransition.SOCIAL_ON, entry.getEnabledOn().getMillis());
                }
            }
        }
    }

    /**
     * Updates a transition timeline using amphiro b1 configuration information.
     *
     * @param transitionTimeline the timeline to update.
     * @param userKey the key of the device whose configuration history is used to extract event timestamps.
     */
    private void updateTimelineWithDeviceConfiguration(TransitionTimeline transitionTimeline, UUID deviceKey) {
        for(DeviceAmphiroConfiguration entry : deviceRepository.getDeviceConfigurationHistory(deviceKey)) {
            switch(entry.getTitle()) {
                case AMPHIRO_OFF_CONFIGURATION:
                    // Since amphiro b1 configuration update acknowledgement
                    // feature may have not been implemented when the device was
                    // paired, we use the device registration date.
                    transitionTimeline.add(EnumTransition.AMPHIRO_PAIRED, entry.getCreatedOn());
                    break;
                default:
                    // If acknowledgement is not available, use the creation timestamp.
                    if((entry.getEnabledOn() == null) || (entry.getCreatedOn() > entry.getEnabledOn())) {
                        transitionTimeline.add(EnumTransition.AMPHIRO_ON, entry.getCreatedOn());
                    } else {
                        transitionTimeline.add(EnumTransition.AMPHIRO_ON, entry.getEnabledOn());
                    }
            }
        }
    }

    /**
     * Extracts a phase timeline from a transition timeline.
     *
     * @param transitionTimeline the transition timeline to used to extract a phase timeline.
     * @return the new phase timeline.
     */
    private PhaseTimeline transitionTimelineToPhaseTimeline(TransitionTimeline transitionTimeline) {
        PhaseTimeline phaseTimeline = new PhaseTimeline();

        // The iterator may return at most four items
        Iterator<Transition> it = transitionTimeline.getIterator();

        Transition current = null;
        Transition previous = null;

        // At least one transition must exist
        if (!it.hasNext()) {
            // Could not resolve phases
            throw new RuntimeException("Transition timeline is empty!");
        } else {
            previous = current = it.next();
        }

        // Transition 1: The first transition must always be AMHIRO_PAIRED
        if (current.getTransition() != EnumTransition.AMPHIRO_PAIRED) {
            // Could not resolve phases
            throw new RuntimeException("Transition [AMHIRO_PAIRED] could not be found.");
        }
        phaseTimeline.add(EnumPhase.BASELINE, current.getTimestamp());


        // Transition 2: (AMHIRO_PAIRED) -> (AMHIRO_ON | MOBILE_ON)
        if (it.hasNext()) {
            current = it.next();

            // Second transition is  (AMHIRO_PAIRED) -> (AMHIRO_ON | MOBILE_ON)
            if (current.getTransition() == EnumTransition.AMPHIRO_ON) {
                phaseTimeline.add(EnumPhase.AMPHIRO_ON, current.getTimestamp());
            } else if (current.getTransition() == EnumTransition.MOBILE_ON) {
                phaseTimeline.add(EnumPhase.MOBILE_ON, current.getTimestamp());
            } else {
                // Should not activate social before mobile
                throw new RuntimeException("Found transition [SOCIAL_ON] without transition [MOBILE_ON].");
            }
        }

        // Transition 3: (AMHIRO_ON) -> (MOBILE_ON) or (MOBILE_ON) -> (AMHIRO_ON | SOCIAL_ON)
        if (it.hasNext()) {
            previous = current;
            current = it.next();

            if ((previous.getTransition() == EnumTransition.AMPHIRO_ON) && (current.getTransition() == EnumTransition.MOBILE_ON)) {
                phaseTimeline.add(EnumPhase.AMPHIRO_ON_MOBILE_ON, current.getTimestamp());
            } else if ((previous.getTransition() == EnumTransition.MOBILE_ON) && (current.getTransition() == EnumTransition.AMPHIRO_ON)) {
                phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_ON, current.getTimestamp());
            } else if ((previous.getTransition() == EnumTransition.MOBILE_ON) && (current.getTransition() == EnumTransition.SOCIAL_ON)) {
                phaseTimeline.add(EnumPhase.MOBILE_ON_SOCIAL_ON, current.getTimestamp());
            } else {
                // Should not activate social before mobile
                throw new RuntimeException(String.format("Invalid transition from [%s] to [%s].", previous.toString(), current.toString()));
            }
        }

        // Transition 4: (MOBILE_ON) -> (SOCIAL_ON) or (SOCIAL_ON) -> (AMHIRO_ON) or (AMHIRO_ON) -> (SOCIAL_ON)
        if(it.hasNext()) {
            previous = current;
            current = it.next();

            if ((previous.getTransition() == EnumTransition.AMPHIRO_ON) && (current.getTransition() == EnumTransition.SOCIAL_ON)) {
                phaseTimeline.add(EnumPhase.MOBILE_ON_AMPHIRO_ON_SOCIAL_ON, current.getTimestamp());
            } else if ((previous.getTransition() == EnumTransition.SOCIAL_ON) && (current.getTransition() == EnumTransition.AMPHIRO_ON)) {
                phaseTimeline.add(EnumPhase.MOBILE_ON_SOCIAL_ON_AMPHIRO_ON, current.getTimestamp());
            } else if ((previous.getTransition() == EnumTransition.MOBILE_ON) && (current.getTransition() == EnumTransition.SOCIAL_ON)) {
                phaseTimeline.add(EnumPhase.AMPHIRO_ON_MOBILE_ON_SOCIAL_ON, current.getTimestamp());
            } else {
                // Should not activate social before mobile
                throw new RuntimeException(String.format("Invalid transition from [%s] to [%s].", previous.toString(), current.toString()));
            }
        }

        return phaseTimeline;

    }

    /**
     * Constructs a phase timeline for a user and amphiro b1 device.
     *
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @return a valid phase timeline.
     * @throws RuntimeException if invalid timeline is detected e.g. phases overlap.
     */
    protected PhaseTimeline constructAmphiroPhaseTimeline(UUID userKey, UUID deviceKey) throws RuntimeException {
        // Get all transitions
        TransitionTimeline transitionTimeline = new TransitionTimeline();

        updateTimelineWithProfile(transitionTimeline, userKey);

        updateTimelineWithDeviceConfiguration(transitionTimeline, deviceKey);

        // Once constructed the timeline, derive phases.
        return transitionTimelineToPhaseTimeline(transitionTimeline);
    }

    /**
     * Constructs a phase timeline for a smart water meter using information
     * from the user profile and all amphiro b1 devices.
     *
     * @param userKey the user key.
     * @return a valid phase timeline.
     * @throws RuntimeException if invalid timeline is detected e.g. phases overlap.
     */
    protected PhaseTimeline constructMeterPhaseTimeline(UUID userKey) throws RuntimeException {
        // Get all transitions
        TransitionTimeline transitionTimeline = new TransitionTimeline();

        updateTimelineWithProfile(transitionTimeline, userKey);

        for (Device d : deviceRepository.getUserDevices(userKey, new DeviceRegistrationQuery())) {
            if(d.getType() == EnumDeviceType.AMPHIRO) {
                updateTimelineWithDeviceConfiguration(transitionTimeline, d.getKey());
            }
        }

        // Once constructed the timeline, derive phases.
        return transitionTimelineToPhaseTimeline(transitionTimeline);
    }

 }
