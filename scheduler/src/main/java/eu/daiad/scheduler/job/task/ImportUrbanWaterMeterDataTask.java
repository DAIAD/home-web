package eu.daiad.scheduler.job.task;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.common.model.loader.FileProcessingStatus;
import eu.daiad.common.model.meter.WaterMeterDataRow;
import eu.daiad.common.service.IWaterMeterDataLoaderService;
import eu.daiad.scheduler.feign.client.UrbanWaterFeignClient;
import eu.daiad.scheduler.model.ubranwater.UrbanWaterCustomer;
import eu.daiad.scheduler.model.ubranwater.UrbanWaterLoginResult;
import eu.daiad.scheduler.model.ubranwater.UrbanWaterMeasurement;
import eu.daiad.scheduler.model.ubranwater.UrbanWaterMeter;
import eu.daiad.scheduler.model.ubranwater.UrbanWaterResponse;
import lombok.Getter;

/**
 * Task for exporting smart water meter data for a utility.
 */
@Component
public class ImportUrbanWaterMeterDataTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ImportUrbanWaterMeterDataTask.class);

    @Autowired
	private ObjectProvider<UrbanWaterFeignClient> client;
    
    @Autowired
    private ObjectMapper objectMapper;
	
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

	private String login(String apiKey, String userName, String password) throws Exception {       
		final String responseContent = client.getObject().login(apiKey, userName, password);

		final UrbanWaterResponse<UrbanWaterLoginResult> loginResponse = objectMapper.readValue(
			responseContent, new TypeReference<UrbanWaterResponse<UrbanWaterLoginResult>>() { }
		);

		if (loginResponse.getStatus() != HttpStatus.OK.value()) {
			throw new Exception(String.format("Login has failed [message=%s]", loginResponse.getMessage()));
		}

		return loginResponse.getData().getAccessToken();
	}

	private List<UrbanWaterCustomer> getCustomers(String apiKey, String accessToken) throws Exception {       
		final String responseContent = client.getObject().getCustomers(apiKey, accessToken, true);

		final UrbanWaterResponse<List<UrbanWaterCustomer>> customerResponse = objectMapper.readValue(
			responseContent, new TypeReference<UrbanWaterResponse<List<UrbanWaterCustomer>>>() { }
		);

		if (customerResponse.getStatus() != HttpStatus.OK.value()) {
			throw new Exception(String.format("Failed to load customers [message=%s]", customerResponse.getMessage()));
		}

		return customerResponse.getData();
	}

	private List<UrbanWaterMeasurement> getDeviceValues(String apiKey, String accessToken, Integer deviceId, long from) throws Exception {       
		final String responseContent = client.getObject().getDeviceValues(apiKey, accessToken, deviceId, from);

		final UrbanWaterResponse<List<UrbanWaterMeasurement>> deviceResponse = objectMapper.readValue(
			responseContent, new TypeReference<UrbanWaterResponse<List<UrbanWaterMeasurement>>>() { }
		);

		if (deviceResponse.getStatus() != HttpStatus.OK.value()) {
			throw new Exception(String.format("Failed to device values [message=%s]", deviceResponse.getMessage()));
		}

		return deviceResponse.getData();
	}
	
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception{
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

			String apiKey   = parameters.get(EnumInParameter.SERVICE_API_KEY.getValue());
			String userName = parameters.get(EnumInParameter.SERVICE_USERNAME.getValue());
			String password = parameters.get(EnumInParameter.SERVICE_PASSWORD.getValue());
			String timezone = parameters.get(EnumInParameter.TIMEZONE.getValue());
			String interval = parameters.get(EnumInParameter.INTERVAL.getValue());
			            
			Assert.isTrue(!StringUtils.isBlank(apiKey), "Expected a non-empty API key");
            Assert.isTrue(!StringUtils.isBlank(userName), "Expected a non-empty username");
            Assert.isTrue(!StringUtils.isBlank(password), "Expected a non-empty password");
            Assert.isTrue(!StringUtils.isBlank(timezone), "Expected a non-empty timezone");
            Assert.isTrue(!StringUtils.isBlank(interval), "Expected a non-empty interval");

            Set<String> zones = DateTimeZone.getAvailableIDs();
            if (!zones.contains(timezone)) {
                throw new Exception(String.format("Time zone [%s] is not supported.", timezone));
            }
            
            final long hours = Long.parseLong(interval);
			final long from  = ZonedDateTime.now()
				.minusHours(hours)			
				.toInstant()
				.getEpochSecond();
				
					
            // Login and get access token
            final String accessToken = this.login(apiKey, userName, password);

			// Load customers
            final List<UrbanWaterCustomer> customers = this.getCustomers(apiKey, accessToken);

			// Load data
			final FileProcessingStatus status = new FileProcessingStatus();

			for (final UrbanWaterCustomer customer : customers) {
				if (!customer.getDevices().isEmpty()) {
					for (final UrbanWaterMeter meter : customer.getDevices()) {
						final List<UrbanWaterMeasurement> data = this.getDeviceValues(
							apiKey, accessToken, meter.getDeviceId(), from
						);
						final List<WaterMeterDataRow> rows = data.stream().map(r-> {
							final String serial = meter.getEui().substring(meter.getEui().length() - 12);

							return WaterMeterDataRow.of(serial, r.getTimestamp() * 1000, r.getVolume());
						}).collect(Collectors.toList());
					
						waterMeterDataLoaderService.importMeterDataToHBase(rows, status, true);
					}
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
    	 * Service API key
    	 */
    	SERVICE_API_KEY("service.api-key"),
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
