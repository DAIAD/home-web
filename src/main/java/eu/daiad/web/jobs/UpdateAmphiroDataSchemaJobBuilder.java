package eu.daiad.web.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroMeasurement;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionDetails;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IAmphiroTimeOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Job that transfers all amphiro b1 data from tables with older schema version
 * to the table with the current schema version.
 */
@Component
public class UpdateAmphiroDataSchemaJobBuilder implements IJobBuilder {
    private static final Log logger = LogFactory.getLog(UpdateAmphiroDataSchemaJobBuilder.class);

    /**
     * Convenient factory for a {@link JobBuilder} for building jobs instances.
     */
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    /**
     * Convenient factory for a {@link StepBuilder} for building job steps.
     */
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing group data.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository devicerepository;

    /**
     * Repository for accessing amphiro b1 data using the schema version 1.
     */
    @Autowired
    @Qualifier("hBaseAmphiroRepositoryV1")
    private IAmphiroTimeOrderedRepository v1Repository;

    /**
     * Repository for accessing amphiro b1 data using the schema version 2.
     */
    @Autowired
    @Qualifier("hBaseAmphiroRepositoryV2")
    private IAmphiroIndexOrderedRepository v2Repository;

    /**
     * Repository for accessing amphiro b1 data using the schema version 3 (current).
     */
    @Autowired
    private IAmphiroIndexOrderedRepository v3Repository;

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
     * Build step for transferring data from schema v1
     *
     * @return the data transfer step.
     */
    private Step transferDataSchema1() {
        return stepBuilderFactory.get("transferDataSchema1").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                long totalUtilities = 0;
                long totalUsers = 0;
                long totalDevices = 0;
                long totalSessions = 0;
                long totalMeasurements = 0;

                try {
                    // Query for devices
                    DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
                    deviceQuery.setType(EnumDeviceType.AMPHIRO);

                    // Query for device sessions
                    AmphiroSessionCollectionTimeIntervalQuery sessionCollectionQuery = new AmphiroSessionCollectionTimeIntervalQuery();
                    sessionCollectionQuery.setStartDate(0L);
                    sessionCollectionQuery.setEndDate(Long.MAX_VALUE);
                    sessionCollectionQuery.setGranularity(0);

                    // Query for single device session with measurements
                    AmphiroSessionTimeIntervalQuery sessionQuery = new AmphiroSessionTimeIntervalQuery();

                    String[] names = { "" };
                    UUID[] deviceKeys = { null };

                    // For every utility
                    for (UtilityInfo utility : utilityRepository.getUtilities()) {
                        totalUtilities++;

                        // For every account
                        for (UUID userKey : groupRepository.getUtilityByKeyMemberKeys(utility.getKey())) {
                            totalUsers++;

                            AuthenticatedUser user = userRepository.getUserByKey(userKey);

                            sessionCollectionQuery.setUserKey(userKey);

                            sessionQuery.setUserKey(userKey);

                            // For every AMPHIRO device
                            for (Device device : devicerepository.getUserDevices(userKey, deviceQuery)) {
                                totalDevices++;

                                deviceKeys[0] = device.getKey();

                                sessionCollectionQuery.setDeviceKey(deviceKeys);

                                if (device.getType().equals(EnumDeviceType.AMPHIRO)) {
                                    AmphiroSessionCollectionTimeIntervalQueryResult result = v1Repository.searchSessions(names,
                                                                                                                         DateTimeZone.forID(user.getTimezone()),
                                                                                                                         sessionCollectionQuery);

                                    if (result.getDevices() != null) {
                                        for (AmphiroSessionCollection collection : result.getDevices()) {
                                            // For every session
                                            for (AmphiroAbstractSession abstractSession : collection.getSessions()) {
                                                totalSessions++;

                                                AmphiroSession session = (AmphiroSession) abstractSession;

                                                sessionQuery.setDeviceKey(device.getKey());
                                                sessionQuery.setSessionId(session.getId());
                                                sessionQuery.setStartDate(session.getTimestamp() - 10000);
                                                sessionQuery.setEndDate(session.getTimestamp() + 10000);

                                                AmphiroSessionTimeIntervalQueryResult data = v1Repository.getSession(sessionQuery);

                                                totalMeasurements += data.getSession().getMeasurements().size();

                                                if (data.getSession() != null) {
                                                    AmphiroMeasurementCollection request = createInsertRequest(deviceKeys[0], data.getSession());

                                                    v3Repository.store(user, (AmphiroDevice) device, request);

                                                    if ((totalSessions % 1000) == 0) {
                                                        logger.info(String.format("V1 to V3: Inserted %d sessions ...", totalSessions));
                                                    }
                                                    if ((totalMeasurements > 0) && ((totalMeasurements % 1000) == 0)) {
                                                        logger.info(String.format("V1 to V3: Inserted %d measurements ...", totalMeasurements));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.fatal("Failed to transfer data from schema v1 tables to schema v3 tables.", ex);

                    throw ex;
                }

                StringBuilder text = new StringBuilder();

                text.append("V1 to V3\n");
                text.append(String.format("Utilities     : %d\n", totalUtilities));
                text.append(String.format("Users         : %d\n", totalUsers));
                text.append(String.format("Devices       : %d\n", totalDevices));
                text.append(String.format("Sessions      : %d\n", totalSessions));
                text.append(String.format("Measurements  : %d\n", totalMeasurements));

                logger.info(text.toString());

                return RepeatStatus.FINISHED;
            }

            @Override
            public void stop() {
                // TODO: Add business logic for stopping processing
            }

        }).build();
    }

    /**
     * Build step for transferring data from schema v2
     *
     * @return the data transfer step.
     */
    private Step transferDataSchema2() {
        return stepBuilderFactory.get("transferDataSchema2").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                long totalUtilities = 0;
                long totalUsers = 0;
                long totalDevices = 0;
                long totalSessions = 0;
                long totalMeasurements = 0;

                try {
                    // Query for devices
                    DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
                    deviceQuery.setType(EnumDeviceType.AMPHIRO);

                    // Query for device sessions
                    AmphiroSessionCollectionIndexIntervalQuery sessionCollectionQuery = new AmphiroSessionCollectionIndexIntervalQuery();
                    sessionCollectionQuery.setStartIndex(0L);
                    sessionCollectionQuery.setEndIndex(Long.MAX_VALUE);
                    sessionCollectionQuery.setType(EnumIndexIntervalQuery.ABSOLUTE);

                    // Query for single device session with measurements
                    AmphiroSessionIndexIntervalQuery sessionQuery = new AmphiroSessionIndexIntervalQuery();

                    String[] names = { "" };
                    UUID[] deviceKeys = { null };

                    // For every utility
                    for (UtilityInfo utility : utilityRepository.getUtilities()) {
                        totalUtilities++;

                        // For every account
                        for (UUID userKey : groupRepository.getUtilityByKeyMemberKeys(utility.getKey())) {
                            totalUsers++;

                            AuthenticatedUser user = userRepository.getUserByKey(userKey);

                            // Set user key for all queries
                            sessionCollectionQuery.setUserKey(userKey);
                            sessionQuery.setUserKey(userKey);

                            // For every AMPHIRO device
                            for (Device device : devicerepository.getUserDevices(userKey, deviceQuery)) {
                                totalDevices++;

                                deviceKeys[0] = device.getKey();

                                sessionCollectionQuery.setDeviceKey(deviceKeys);

                                if (device.getType().equals(EnumDeviceType.AMPHIRO)) {
                                    AmphiroSessionCollectionIndexIntervalQueryResult result = v2Repository.getSessions(names,
                                                                                                                       DateTimeZone.forID(user.getTimezone()),
                                                                                                                       sessionCollectionQuery);

                                    if (result.getDevices() != null) {
                                        for (AmphiroSessionCollection collection : result.getDevices()) {
                                            // For every session
                                            for (AmphiroAbstractSession abstractSession : collection.getSessions()) {
                                                totalSessions++;

                                                AmphiroSession session = (AmphiroSession) abstractSession;

                                                sessionQuery.setDeviceKey(device.getKey());
                                                sessionQuery.setSessionId(session.getId());
                                                sessionQuery.setExcludeMeasurements(false);

                                                AmphiroSessionIndexIntervalQueryResult data = v2Repository.getSession(sessionQuery);

                                                totalMeasurements += data.getSession().getMeasurements().size();

                                                if (data.getSession() != null) {
                                                    AmphiroMeasurementCollection request = createInsertRequest(deviceKeys[0], data.getSession());

                                                    v3Repository.store(user, (AmphiroDevice) device, request);

                                                    if ((totalSessions % 1000) == 0) {
                                                        logger.info(String.format("V2 to V3: Inserted %d sessions ...", totalSessions));
                                                    }
                                                    if ((totalMeasurements > 0) && ((totalMeasurements % 1000) == 0)) {
                                                        logger.info(String.format("V2 to V3: Inserted %d measurements ...", totalMeasurements));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.fatal("Failed to transfer data from schema v2 tables to schema v3 tables.", ex);

                    throw ex;
                }

                StringBuilder text = new StringBuilder();

                text.append("V2 to V3\n");
                text.append(String.format("Utilities     : %d\n", totalUtilities));
                text.append(String.format("Users         : %d\n", totalUsers));
                text.append(String.format("Devices       : %d\n", totalDevices));
                text.append(String.format("Sessions      : %d\n", totalSessions));
                text.append(String.format("Measurements  : %d\n", totalMeasurements));

                logger.info(text.toString());

                return RepeatStatus.FINISHED;
            }

            @Override
            public void stop() {
                // TODO: Add business logic for stopping processing
            }

        }).build();
    }

    /**
     * Build a data transfer job.
     *
     * @param name the job name.
     * @param incrementer the job parameter incrementer used for generating
     *                    unique job execution instances.
     */
    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .start(transferDataSchema2())
                                .build();
    }
}
