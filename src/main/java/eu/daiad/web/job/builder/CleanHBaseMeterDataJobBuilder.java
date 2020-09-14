package eu.daiad.web.job.builder;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
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
import eu.daiad.web.model.meter.MeterDataStoreStats;
import eu.daiad.web.model.meter.WaterMeterDataPoint;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IMeterDataRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Job that transfers all amphiro b1 data from tables with older schema version
 * to the table with the current schema version.
 */
@Component
public class CleanHBaseMeterDataJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(CleanHBaseMeterDataJobBuilder.class);

    /**
     * Recomputes the difference between consecutive readings.
     */
    private static final String STEP_UPDATE_DIFFERENCE = "update-difference-field";

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository devicerepository;

    /**
     * Repository for accessing smart water meter data.
     */
    @Autowired
    private IMeterDataRepository meterDataRepository;

    /**
     * Recomputes the difference between consecutive readings.
     *
     * @return the data transfer step.
     */
    private Step updateDifferenceCellValue() {
        return stepBuilderFactory.get(STEP_UPDATE_DIFFERENCE).tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                long totalUtilities = 0;
                long totalUsers = 0;
                long totalMeters = 0;
                long totalRows = 0;
                long totalUpdates = 0;

                try {
                    // Query for meter
                    DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
                    deviceQuery.setType(EnumDeviceType.METER);

                    // Query for meter readings
                    WaterMeterMeasurementQuery waterMeterMeasurementQuery = new WaterMeterMeasurementQuery();
                    waterMeterMeasurementQuery.setStartDate(0L);
                    waterMeterMeasurementQuery.setEndDate(Long.MAX_VALUE);
                    waterMeterMeasurementQuery.setGranularity(0);

                    String[] serials = { "" };
                    UUID[] meterKeys = { null };

                    // For every utility
                    for (UtilityInfo utility : utilityRepository.getUtilities()) {
                        totalUtilities++;

                        // For every account
                        for (UUID userKey : utilityRepository.getMembers(utility.getKey())) {
                            totalUsers++;
                            waterMeterMeasurementQuery.setUserKey(userKey);

                            // For every meter
                            for (Device device : devicerepository.getUserDevices(userKey, deviceQuery)) {
                                totalMeters++;
                                WaterMeterDevice meterDevice = (WaterMeterDevice) device;


                                serials[0] = meterDevice.getSerial();
                                meterKeys[0] = device.getKey();

                                waterMeterMeasurementQuery.setDeviceKey(meterKeys);

                                if (device.getType().equals(EnumDeviceType.METER)) {
                                    WaterMeterMeasurementQueryResult result = meterDataRepository.searchMeasurements(serials, DateTimeZone.UTC, waterMeterMeasurementQuery);

                                    if (!result.getSeries().isEmpty()) {
                                        for (WaterMeterDataSeries series : result.getSeries()) {
                                            WaterMeterMeasurementCollection data = new WaterMeterMeasurementCollection();
                                            data.setDeviceKey(device.getKey());
                                            for(WaterMeterDataPoint point : series.getValues()){
                                                data.add(point.getTimestamp(), point.getVolume(), point.getDifference());
                                                totalRows++;
                                            }

                                            MeterDataStoreStats stats = meterDataRepository.store(meterDevice.getSerial(), data);
                                            totalUpdates +=stats.getUpdated();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.fatal("Failed to update HBase meter data.", ex);

                    throw ex;
                }

                StringBuilder text = new StringBuilder();

                text.append("HBase Meter Data Update\n");
                text.append(String.format("Utilities     : %d\n", totalUtilities));
                text.append(String.format("Users         : %d\n", totalUsers));
                text.append(String.format("Meters        : %d\n", totalMeters));
                text.append(String.format("Rows          : %d\n", totalRows));
                text.append(String.format("Updates       : %d\n", totalUpdates));

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
     * @param incrementer the job parameter incrementer used for generating unique job execution instances.
     */
    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name).incrementer(incrementer).start(updateDifferenceCellValue()).build();
    }
}
