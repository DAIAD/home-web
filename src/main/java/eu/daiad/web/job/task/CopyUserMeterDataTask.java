package eu.daiad.web.job.task;

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

import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.meter.WaterMeterDataPoint;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IMeterDataRepository;
import eu.daiad.web.repository.application.IUserRepository;

/**
 * Task for copying meter data between two users.
 */
@Component
public class CopyUserMeterDataTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(CopyUserMeterDataTask.class);

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
     * Repository for accessing smart water meter data.
     */
    @Autowired
    private IMeterDataRepository meterDataRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

        // Get source properties
        UUID sourceUserKey = UUID.fromString(parameters.get(EnumInParameter.SOURCE_USER_KEY.getValue()));
        String sourceUserName = parameters.get(EnumInParameter.SOURCE_USER_NAME.getValue());
        UUID sourceMeterKey = UUID.fromString(parameters.get(EnumInParameter.SOURCE_METER_KEY.getValue()));
        String sourceMeterSerial = parameters.get(EnumInParameter.SOURCE_METER_SERIAL.getValue());

        // Get target properties
        UUID targetUserKey = UUID.fromString(parameters.get(EnumInParameter.TARGET_USER_KEY.getValue()));
        String targetUserName = parameters.get(EnumInParameter.TARGET_USER_NAME.getValue());
        UUID targetMeterKey = null;
        String targetMeterSerial = parameters.get(EnumInParameter.TARGET_METER_SERIAL.getValue());

        if (sourceUserKey.equals(targetUserKey)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source and target user are the same.");
        }
        if (sourceMeterSerial.equalsIgnoreCase(targetMeterSerial)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source and target meter serial numbers are the same.");
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

        // Get meters
        WaterMeterDevice sourceMeter = getMeter(sourceUserKey, sourceMeterKey);
        if (sourceMeter == null) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source meter was not found.");
        } else if(!sourceMeter.getSerial().equals(sourceMeterSerial)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source meter serial does not match.");
        }
        if (deviceRepository.getWaterMeterDeviceBySerial(targetMeterSerial) != null) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The target meter serial number already exists.");
        }
        if (hasMeters(targetUserKey)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The target account has already a meter assigned to it.");
        }

        // Create target meter
        targetMeterKey = deviceRepository.createMeterDevice(targetUser.getUsername(),
                                                            targetMeterSerial,
                                                            sourceMeter.getProperties(),
                                                            sourceMeter.getLocation());
        if (sourceMeterKey.equals(targetMeterKey)) {
            throw ApplicationException.create(SharedErrorCode.UNKNOWN, "The source and target meter are the same.");
        }

        // Get source data
        WaterMeterMeasurementQuery waterMeterMeasurementQuery = new WaterMeterMeasurementQuery();
        waterMeterMeasurementQuery.setStartDate(0L);
        waterMeterMeasurementQuery.setEndDate(Long.MAX_VALUE);
        waterMeterMeasurementQuery.setGranularity(0);
        waterMeterMeasurementQuery.setUserKey(sourceUserKey);
        waterMeterMeasurementQuery.setDeviceKey(new UUID[] { sourceMeter.getKey() });

        WaterMeterMeasurementQueryResult result = meterDataRepository.searchMeasurements(new String[] { sourceMeter.getSerial() },
                                                                                         DateTimeZone.UTC,
                                                                                         waterMeterMeasurementQuery);

        WaterMeterDataSeries series = result.getSeries().get(0);

        WaterMeterMeasurementCollection data = new WaterMeterMeasurementCollection();
        data.setDeviceKey(targetMeterKey);
        for(WaterMeterDataPoint point : series.getValues()){
            data.add(point.getTimestamp(), point.getVolume(), point.getDifference());
        }

        meterDataRepository.store(targetMeterSerial, data);

        StringBuilder text = new StringBuilder();
        text.append(String.format("Copied data from [%s - %s] to [%s - %s]. ", sourceUserName, sourceMeterSerial, targetUserName, targetMeterSerial));
        text.append(String.format("Total points inserted    : %d\n", data.getMeasurements().size()));
        logger.info(text.toString());

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Find meter by user and meter key.
     *
     * @param userKey the user key.
     * @param meterKey the meter key.
     * @return an instance of {@link WaterMeterDevice}.
     */
    private WaterMeterDevice getMeter(UUID userKey, UUID meterKey) {
        DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
        deviceQuery.setType(EnumDeviceType.METER);

        for (Device device : deviceRepository.getUserDevices(userKey, deviceQuery)) {
            if (device.getKey().equals(meterKey)) {
                return (WaterMeterDevice) device;
            }
        }

        return null;
    }

    /**
     * Checks if any meter is assigned to an existing account.
     *
     * @param userKey the user key.
     * @return true if a meter is assigned to the user.
     */
    private boolean hasMeters(UUID userKey) {
        return (deviceRepository.getUserDevices(userKey, EnumDeviceType.METER).size() > 0);
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
         * Source meter key.
         */
        SOURCE_METER_KEY("source.meter.key"),
        /**
         * Source meter serial number.
         */
        SOURCE_METER_SERIAL("source.meter.serial"),
        /**
         * Target user key.
         */
        TARGET_USER_KEY("target.user.key"),
        /**
         * Source user key.
         */
        TARGET_USER_NAME("target.user.name"),
        /**
         * Target meter serial number.
         */
        TARGET_METER_SERIAL("target.meter.serial");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }
}
