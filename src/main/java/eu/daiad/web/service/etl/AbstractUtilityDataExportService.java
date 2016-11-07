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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
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
        switch(query.getSource()) {
            case METER:
                exportMeterPhaseTimestamps(query, result);
                break;
            case AMPHIRO:
                exportAmphiroPhaseTimestamps(query, result);
                break;
            case NONE:
                // Ignore
                break;
        }
    }


    /**
     * Exports phase start/end timestamp for amphiro b1.
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportAmphiroPhaseTimestamps(UtilityDataExportQuery query, ExportResult result) throws IOException {
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
                for (Device d : deviceRepository.getUserDevices(user.getKey(), new DeviceRegistrationQuery())) {
                    if (d.getType() == EnumDeviceType.AMPHIRO) {
                        try {
                            PhaseTimeline phaseTimeline = constructAmphiroPhaseTimeline(user.getKey(), d.getKey());

                            row = new ArrayList<String>();

                            row.add(user.getKey().toString());
                            row.add(user.getUsername());
                            row.add(d.getKey().toString());
                            row.add(((AmphiroDevice) d).getName());

                            createPhaseRowWithTimestamps(EnumPhase.BASELINE, row, phaseTimeline, formatter);
                            if(phaseTimeline.getPhase(EnumPhase.MOBILE_ON_AMPHIRO_OFF) != null) {
                                createPhaseRowWithTimestamps(EnumPhase.MOBILE_ON_AMPHIRO_OFF, row, phaseTimeline, formatter);
                            } else if(phaseTimeline.getPhase(EnumPhase.MOBILE_OFF_AMPHIRO_ON) != null) {
                                createPhaseRowWithTimestamps(EnumPhase.MOBILE_OFF_AMPHIRO_ON, row, phaseTimeline, formatter);
                            } else {
                                row.add("");
                                row.add("");
                                row.add("");
                            }
                            if(phaseTimeline.getPhase(EnumPhase.MOBILE_ON_AMPHIRO_ON) != null) {
                                createPhaseRowWithTimestamps(EnumPhase.MOBILE_ON_AMPHIRO_ON, row, phaseTimeline, formatter);
                            } else {
                                row.add("");
                                row.add("");
                                row.add("");
                            }

                            totalRows++;
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

        result.increment(totalRows);
        result.getFiles().add(new FileLabelPair(new File(filename), "phase-timestamp.csv", totalRows));
    }

    /**
     * Exports phase start/end timestamp for smart water meters.
     *
     * @param query the query that selects the data to export.
     * @param result export result.
     * @return total rows exported.
     * @throws IOException if file creation fails.
     */
    private void exportMeterPhaseTimestamps(UtilityDataExportQuery query, ExportResult result) throws IOException {
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
        row.add("meter key");
        row.add("meter serial");

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
                    if(d.getType() == EnumDeviceType.METER) {
                        try {
                            PhaseTimeline phaseTimeline = constructMeterPhaseTimeline(user.getKey());

                            row = new ArrayList<String>();

                            row.add(user.getKey().toString());
                            row.add(user.getUsername());
                            row.add(d.getKey().toString());
                            row.add(((WaterMeterDevice) d).getSerial());

                            createPhaseRowWithTimestamps(EnumPhase.BASELINE, row, phaseTimeline, formatter);
                            if(phaseTimeline.getPhase(EnumPhase.MOBILE_ON_AMPHIRO_OFF) != null) {
                                createPhaseRowWithTimestamps(EnumPhase.MOBILE_ON_AMPHIRO_OFF, row, phaseTimeline, formatter);
                            } else if(phaseTimeline.getPhase(EnumPhase.MOBILE_OFF_AMPHIRO_ON) != null) {
                                createPhaseRowWithTimestamps(EnumPhase.MOBILE_OFF_AMPHIRO_ON, row, phaseTimeline, formatter);
                            } else {
                                row.add("");
                                row.add("");
                                row.add("");
                            }
                            if(phaseTimeline.getPhase(EnumPhase.MOBILE_ON_AMPHIRO_ON) != null) {
                                createPhaseRowWithTimestamps(EnumPhase.MOBILE_ON_AMPHIRO_ON, row, phaseTimeline, formatter);
                            } else {
                                row.add("");
                                row.add("");
                                row.add("");
                            }

                            totalRows++;
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

        result.increment(totalRows);
        result.getFiles().add(new FileLabelPair(new File(filename), "phase-timestamp.csv", totalRows));
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
    }

    /**
     * Extracts a phase timeline from a transition timeline.
     *
     * @param transitionTimeline the transition timeline to used to extract a phase timeline.
     * @return the new phase timeline.
     */
    private PhaseTimeline transitionTimelineToPhaseTimeline(TransitionTimeline transitionTimeline) {
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
    protected static enum EnumPhase {
        BASELINE, MOBILE_OFF_AMPHIRO_ON, MOBILE_ON_AMPHIRO_OFF, MOBILE_ON_AMPHIRO_ON, SOCIAL_ON;
    }

    /**
     * Represents a phase in trial.
     */
    protected static class Phase {

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
    protected static class PhaseTimeline {

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

}
