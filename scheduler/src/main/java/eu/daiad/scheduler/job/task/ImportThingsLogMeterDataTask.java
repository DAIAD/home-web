package eu.daiad.scheduler.job.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.loader.FileProcessingStatus;
import eu.daiad.common.model.meter.WaterMeterDataRow;
import eu.daiad.common.repository.application.IDeviceRepository;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.service.IWaterMeterDataLoaderService;
import eu.daiad.scheduler.feign.client.ThingsLogFeignClient;
import eu.daiad.scheduler.model.thingslog.ThingsLogLoginRequest;
import eu.daiad.scheduler.model.thingslog.ThingsLogMeasurement;
import feign.FeignException;
import lombok.Getter;

/**
 * Task for importing smart water meter data from https://thingslog.com/
 */
@Component
public class ImportThingsLogMeterDataTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ImportThingsLogMeterDataTask.class);

    @Autowired
	private ObjectProvider<ThingsLogFeignClient> client;
    
    @Autowired
    private IUserRepository userRepository;
    
    @Autowired
    private IDeviceRepository deviceRepository;
	
    /**
     * Entity manager for persisting upload meta data.
     */
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Service for downloading, parsing and importing smart water meter data to HBase.
     */
    @Autowired
    private IWaterMeterDataLoaderService waterMeterDataLoaderService;

	private String login(String username, String password) throws Exception {       
		final ResponseEntity<Void> loginResponse = client.getObject().login(ThingsLogLoginRequest.of(username, password));

		return loginResponse.getHeaders().get("Authorization").get(0);
	}

	private List<ThingsLogMeasurement> getDeviceValues(String token, String deviceId, String fromDate, String toDate) throws Exception {
		try {
			final ResponseEntity<List<ThingsLogMeasurement>> deviceResponse = client.getObject().getDeviceValues(
				token, deviceId, fromDate, toDate
			);
	
			final List<ThingsLogMeasurement> result = deviceResponse.getBody();
	
			return result;
		} catch (FeignException fex) {
			logger.error(String.format(
				"Failed to load device values. [deviceId=%d, message=%s]", 
				deviceId, fex.getMessage()
			));
			return Collections.emptyList();
		}
	}
	
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception{
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            String utilityId = parameters.get(EnumInParameter.UTILITY_ID.getValue());
			String userName = parameters.get(EnumInParameter.SERVICE_USERNAME.getValue());
			String password = parameters.get(EnumInParameter.SERVICE_PASSWORD.getValue());
			String timezone = parameters.get(EnumInParameter.TIMEZONE.getValue());
			String interval = parameters.get(EnumInParameter.INTERVAL.getValue());
			            
            Assert.isTrue(!StringUtils.isBlank(userName), "Expected a non-empty username");
            Assert.isTrue(!StringUtils.isBlank(password), "Expected a non-empty password");
            Assert.isTrue(!StringUtils.isBlank(timezone), "Expected a non-empty timezone");
            Assert.isTrue(!StringUtils.isBlank(interval), "Expected a non-empty interval");

            Set<String> zones = DateTimeZone.getAvailableIDs();
            if (!zones.contains(timezone)) {
                throw new Exception(String.format("Time zone [%s] is not supported.", timezone));
            }
            
            List<UUID> userKeys = userRepository.getUserKeysForUtility(Integer.parseInt(utilityId));
            
			final LocalDateTime now = LocalDateTime.now();
			final LocalDateTime from = now.minusHours(Integer.parseInt(interval)).withHour(0).withMinute(0).withSecond(0).withNano(0);
			final LocalDateTime to = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
					
            // Login and get access token
            final String token = this.login(userName, password);
		
			// Load data
			final FileProcessingStatus status = new FileProcessingStatus();

			for (final UUID key : userKeys) {
				final List<Device> devices = this.deviceRepository.getUserDevices(key, EnumDeviceType.METER);

				for (Device device : devices) {
					final WaterMeterDevice meter = (WaterMeterDevice) device;
					final String serial = meter.getSerial().substring(3, meter.getSerial().length());

					final List<ThingsLogMeasurement> data = this.getDeviceValues(
						token, serial, 
						from.format(DateTimeFormatter.ISO_DATE_TIME), 
						to.format(DateTimeFormatter.ISO_DATE_TIME)
					);

					final List<WaterMeterDataRow> rows = data.stream()
						.map(r -> {
							return WaterMeterDataRow.of(
								meter.getSerial(), 
								r.getDate().toInstant().toEpochMilli(), 
								r.getReading() * 1000
							);
						})
						.collect(Collectors.toList());
				
					waterMeterDataLoaderService.importMeterDataToHBase(rows, status, true);
				}
			}
			
			logger.info(status.toString());
        } catch (Exception ex) {
            logger.fatal("Failed to import meter data to HBASE.", ex);

            throw ex;
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {

    }

    /**
     * Enumeration of task input parameters.
     */
    public static enum EnumInParameter {
    	/**
    	 * Utility id
    	 */
    	UTILITY_ID("utility.id"),
        /**
         * Service user name
         */
        SERVICE_USERNAME("service.username"),
        /**
         * Service password
         */
        SERVICE_PASSWORD("service.password"),
        /**
         * Interval in hours to query
         */
        INTERVAL("interval"),
        /**
         * Utility time zone
         */
        TIMEZONE("timezone")
        ;

    	@Getter
        private final String value;

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

}
