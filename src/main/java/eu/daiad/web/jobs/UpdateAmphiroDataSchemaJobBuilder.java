package eu.daiad.web.jobs;

import java.util.ArrayList;
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
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroMeasurement;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQueryResult;
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

@Component
public class UpdateAmphiroDataSchemaJobBuilder implements IJobBuilder {
	private static final Log logger = LogFactory.getLog(UpdateAmphiroDataSchemaJobBuilder.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private IUtilityRepository utilityRepository;

	@Autowired
	private IGroupRepository groupRepository;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository devicerepository;

	@Autowired
	private IAmphiroTimeOrderedRepository amphiroTimeOrderedRepository;

	@Autowired
	private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

	public UpdateAmphiroDataSchemaJobBuilder() {

	}

	private AmphiroMeasurementCollection createInsertRequest(UUID deviceKey, AmphiroSessionTimeIntervalQueryResult data) {
		AmphiroMeasurementCollection request = new AmphiroMeasurementCollection();
		request.setDeviceKey(deviceKey);

		// Data collections
		ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();
		ArrayList<AmphiroMeasurement> measurements = new ArrayList<AmphiroMeasurement>();

		// Create a single session
		AmphiroSession session = new AmphiroSession();

		session.setDuration(data.getSession().getDuration());
		session.setEnergy(data.getSession().getEnergy());
		session.setFlow(data.getSession().getFlow());
		session.setHistory(data.getSession().isHistory());
		session.setId(data.getSession().getId());
		session.setTemperature(data.getSession().getTemperature());
		session.setVolume(data.getSession().getVolume());
		session.setTimestamp(data.getSession().getTimestamp());

		sessions.add(session);
		request.setSessions(sessions);

		// Create measurements
		for (AmphiroMeasurement m : data.getSession().getMeasurements()) {
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
		request.setMeasurements(measurements);

		return request;
	}

	private Step transferData() {
		return stepBuilderFactory.get("transferData").tasklet(new StoppableTasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
				long totalUtilities = 0;
				long totalUsers = 0;
				long totalDevices = 0;
				long totalSessions = 0;
				long totalMeasurements = 0;

				try {
					DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
					deviceQuery.setType(EnumDeviceType.AMPHIRO);

					AmphiroSessionCollectionTimeIntervalQuery sessionCollectionQuery = new AmphiroSessionCollectionTimeIntervalQuery();
					sessionCollectionQuery.setStartDate(0L);
					sessionCollectionQuery.setEndDate(Long.MAX_VALUE);
					sessionCollectionQuery.setGranularity(0);

					String[] names = { "" };
					UUID[] deviceKeys = { null };

					AmphiroSessionTimeIntervalQuery sessionQuery = new AmphiroSessionTimeIntervalQuery();

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
									AmphiroSessionCollectionTimeIntervalQueryResult result = amphiroTimeOrderedRepository
													.searchSessions(names, DateTimeZone.forID(user.getTimezone()),
																	sessionCollectionQuery);

									if (result.getDevices() != null) {
										for (AmphiroSessionCollection collection : result.getDevices()) {
											for (AmphiroAbstractSession abstractSession : collection.getSessions()) {
												totalSessions++;

												AmphiroSession session = (AmphiroSession) abstractSession;

												sessionQuery.setDeviceKey(device.getKey());
												sessionQuery.setSessionId(session.getId());
												sessionQuery.setStartDate(session.getTimestamp() - 10000);
												sessionQuery.setEndDate(session.getTimestamp() + 10000);

												AmphiroSessionTimeIntervalQueryResult data = amphiroTimeOrderedRepository
																.getSession(sessionQuery);

												totalMeasurements += data.getSession().getMeasurements().size();

												AmphiroMeasurementCollection request = createInsertRequest(
																deviceKeys[0], data);

												if ((totalSessions % 1000) == 0) {
													logger.info(String
																	.format("Inserted %d sessions ...", totalSessions));
												}
												if ((totalMeasurements > 0) && ((totalMeasurements % 1000) == 0)) {
													logger.info(String.format("Inserted %d sessions ...",
																	totalMeasurements));
												}

												amphiroIndexOrderedRepository.storeData(userKey, request);
											}
										}
									}
								}
							}
						}
					}
				} catch (Exception ex) {
					logger.fatal("Failed to transfer data from schema v1 tables to schema v2 tables.", ex);

					throw ex;
				}

				logger.info(String.format("Utilities     : %d", totalUtilities));
				logger.info(String.format("Users         : %d", totalUsers));
				logger.info(String.format("Devices       : %d", totalDevices));
				logger.info(String.format("Sessions      : %d", totalSessions));
				logger.info(String.format("Measurements  : %d", totalMeasurements));

				return RepeatStatus.FINISHED;
			}

			@Override
			public void stop() {
				// TODO: Add business logic for stopping processing
			}

		}).build();
	}

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name).incrementer(incrementer).start(transferData()).build();
	}
}
