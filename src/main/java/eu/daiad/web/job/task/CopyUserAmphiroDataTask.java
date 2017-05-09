package eu.daiad.web.job.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroMeasurement;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionDetails;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;

/**
 * Task for copying amphiro b1 data between two users. Only showers and time
 * series for real-time ones are copied. The ignore and member fields are not
 * copied.
 */
@Component
public class CopyUserAmphiroDataTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(CopyUserAmphiroDataTask.class);

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Repository for accessing amphiro b1 data.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

        // Get source properties
        UUID sourceUserKey = UUID.fromString(parameters.get(EnumInParameter.SOURCE_USER_KEY.getValue()));
        String sourceUserName = parameters.get(EnumInParameter.SOURCE_USER_NAME.getValue());
        UUID sourceAmphiroKey = UUID.fromString(parameters.get(EnumInParameter.SOURCE_AMPHIRO_KEY.getValue()));

        // Get target properties
        UUID targetUserKey = UUID.fromString(parameters.get(EnumInParameter.TARGET_USER_KEY.getValue()));
        String targetUserName = parameters.get(EnumInParameter.TARGET_USER_NAME.getValue());

        if (sourceUserKey.equals(targetUserKey)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source and target user are the same.");
        }
        if (sourceUserName.equalsIgnoreCase(targetUserName)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source and target user names are the same.");
        }

        // Get users
        AuthenticatedUser sourceUser = userRepository.getUserByKey(sourceUserKey);
        if(sourceUser == null) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source user was not found.");
        } else if (!sourceUser.getUsername().equals(sourceUserName)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source user key does not match the user name.");
        }
        AuthenticatedUser targetUser = userRepository.getUserByKey(targetUserKey);
        if(targetUser == null) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The target user was not found.");
        } else if (!targetUser.getUsername().equals(targetUserName)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The target user key does not match the user name.");
        }

        // Get amphiro b1 devices
        AmphiroDevice sourceAmphiro = getAmphiro(sourceUserKey, sourceAmphiroKey);
        if (sourceAmphiro == null) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source amphiro b1 was not found.");
        }
        if (hasAmphiro(targetUserKey)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The target account has already an amphiro b1 assigned to it.");
        }

        // Create target amphiro b1
        AmphiroDevice targetAmphiro = deviceRepository.createAmphiroDevice(targetUser.getKey(),
                                                                           sourceAmphiro.getName(),
                                                                           sourceAmphiro.getMacAddress(),
                                                                           sourceAmphiro.getAesKey(),
                                                                           sourceAmphiro.getProperties());

        if (sourceAmphiroKey.equals(targetAmphiro.getKey())) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source and target amphiro b1 are the same.");
        }

        // Get source data
        AmphiroSessionCollectionIndexIntervalQuery showerQuery = new AmphiroSessionCollectionIndexIntervalQuery();
        showerQuery.setStartIndex(0L);
        showerQuery.setLength(Integer.MAX_VALUE);
        showerQuery.setType(EnumIndexIntervalQuery.SLIDING);
        showerQuery.setDeviceKey(new UUID[] { sourceAmphiro.getKey() });
        showerQuery.setUserKey(sourceUserKey);

        AmphiroSessionCollectionIndexIntervalQueryResult result = amphiroIndexOrderedRepository.getSessions(new String[] { sourceAmphiro.getName() },
                                                                                                            DateTimeZone.UTC,
                                                                                                            showerQuery);

        AmphiroSessionIndexIntervalQuery sessionQuery = new AmphiroSessionIndexIntervalQuery();
        for (AmphiroAbstractSession abstractSession : result.getDevices().get(0).getSessions()) {
            AmphiroSession session = (AmphiroSession) abstractSession;

            sessionQuery.setUserKey(sourceUserKey);
            sessionQuery.setDeviceKey(sourceAmphiro.getKey());
            sessionQuery.setSessionId(session.getId());
            sessionQuery.setExcludeMeasurements(false);

            AmphiroSessionIndexIntervalQueryResult data = amphiroIndexOrderedRepository.getSession(sessionQuery);

            if (data.getSession() != null) {
                AmphiroMeasurementCollection request = createInsertRequest(targetAmphiro.getKey(), data.getSession());

                amphiroIndexOrderedRepository.store(targetUser, targetAmphiro, request);
            }
        }

        StringBuilder text = new StringBuilder();
        text.append(String.format("Copied data from [%s - %s] to [%s - %s]. ", sourceUserName, sourceAmphiroKey, targetUserName, targetAmphiro.getKey()));
        text.append(String.format("Total showers inserted    : %d\n", result.getDevices().get(0).getSessions().size()));
        logger.info(text.toString());

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Find amphiro b1 by user and device key.
     *
     * @param userKey the user key.
     * @param amphiroKey the amphiro key.
     * @return an instance of {@link AmphiroDevice}.
     */
    private AmphiroDevice getAmphiro(UUID userKey, UUID amphiroKey) {
        DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
        deviceQuery.setType(EnumDeviceType.AMPHIRO);

        for (Device device : deviceRepository.getUserDevices(userKey, deviceQuery)) {
            if (device.getKey().equals(amphiroKey)) {
                return (AmphiroDevice) device;
            }
        }

        return null;
    }

    /**
     * Checks if any amphiro b1 is assigned to an existing account.
     *
     * @param userKey the user key.
     * @return if an amphiro b1 is registered to the user account.
     */
    private boolean hasAmphiro(UUID userKey) {
        return (deviceRepository.getUserDevices(userKey, EnumDeviceType.AMPHIRO).size() > 0);
    }

    /**
     * Creates a generic insert request for amphiro b1 data.
     *
     * @param deviceKey the device key.
     * @param data the data to import.
     * @return A valid amphiro b1 data request.
     */
    private AmphiroMeasurementCollection createInsertRequest(UUID deviceKey, AmphiroSessionDetails data) {
        AmphiroMeasurementCollection request = new AmphiroMeasurementCollection();
        request.setDeviceKey(deviceKey);

        // Data collections
        ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();
        ArrayList<AmphiroMeasurement> measurements = new ArrayList<AmphiroMeasurement>();

        // Create a single session
        AmphiroSession session = new AmphiroSession();

        session.setDuration(data.getDuration());
        session.setEnergy(data.getEnergy());
        session.setFlow(data.getFlow());
        session.setHistory(data.isHistory());
        session.setId(data.getId());
        session.setTemperature(data.getTemperature());
        session.setVolume(data.getVolume());
        session.setTimestamp(data.getTimestamp());

        for(KeyValuePair kvp : data.getProperties()) {
            session.addProperty(kvp.getKey(), kvp.getValue());
        }

        sessions.add(session);
        request.setSessions(sessions);

        // Skip measurements for historical sessions
        if ((!session.isHistory()) &&
            (data.getMeasurements() != null) &&
            (!data.getMeasurements().isEmpty())) {

            // Create measurements
            for (AmphiroMeasurement m : data.getMeasurements()) {
                AmphiroMeasurement measurement = new AmphiroMeasurement();

                measurement.setEnergy(m.getEnergy());
                measurement.setHistory(m.isHistory());
                measurement.setIndex(m.getIndex());
                measurement.setSessionId(m.getSessionId());
                measurement.setTemperature(m.getTemperature());
                measurement.setTimestamp(m.getTimestamp());
                measurement.setVolume(m.getVolume());

                measurements.add(measurement);
            }

            // Sort measurements. Sorting using indexes and timestamps is valid
            // only for schema v1 and v2.
            Collections.sort(measurements, new Comparator<AmphiroMeasurement>() {

                @Override
                public int compare(AmphiroMeasurement m1, AmphiroMeasurement m2) {
                    if (m1.getIndex() < m2.getIndex()) {
                        return -1;
                    }
                    if (m1.getIndex() > m2.getIndex()) {
                        return 1;
                    }

                    if (m1.getTimestamp() < m2.getTimestamp()) {
                        return -1;
                    }
                    if (m1.getTimestamp() > m2.getTimestamp()) {
                        return 1;
                    }
                    return 0;
                }
            });

            // Eliminate duplicates
            int duplicates = 0;
            for (int i = measurements.size() - 1; i > 0; i--) {
                if (measurements.get(i).getIndex() == measurements.get(i - 1).getIndex()) {
                    measurements.remove(i);
                    duplicates++;
                }
            }
            if (duplicates > 0) {
                logger.info(String.format("Eliminated %d duplicates from session %d", duplicates, session.getId()));
            }

            // Compute aggregates
            for (int i = 0, count = measurements.size() - 1; i < count; i++) {
                // Set volume
                measurements.get(i + 1).setVolume(measurements.get(i).getVolume() +
                                                  measurements.get(i + 1).getVolume());
                // Set energy
                measurements.get(i + 1).setEnergy(measurements.get(i).getEnergy() +
                                                  measurements.get(i + 1).getEnergy());
            }

        }

        // Set measurements
        request.setMeasurements(measurements);

        return request;
    }

    /**
     * Enumeration of task input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Source user key.
         */
        SOURCE_USER_KEY("source.user.key"),
        /**
         * Source user name.
         */
        SOURCE_USER_NAME("source.user.name"),
        /**
         * Source amphiro b1 key.
         */
        SOURCE_AMPHIRO_KEY("source.amphiro.key"),
        /**
         * Target user key.
         */
        TARGET_USER_KEY("target.user.key"),
        /**
         * Source user key.
         */
        TARGET_USER_NAME("target.user.name");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }
}
