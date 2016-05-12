package eu.daiad.web.service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.web.domain.application.GroupCluster;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.Error;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.QueryErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.query.ClusterPopulationFilter;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumClusterType;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumRankingType;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.GroupPopulationFilter;
import eu.daiad.web.model.query.PopulationFilter;
import eu.daiad.web.model.query.UserPopulationFilter;
import eu.daiad.web.model.query.UtilityPopulationFilter;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAmphiroTimeOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

@Service
public class DataService implements IDataService {

	@Autowired
	protected MessageSource messageSource;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	IAmphiroTimeOrderedRepository amphiroRepository;

	@Autowired
	IWaterMeterMeasurementRepository meterRepository;

	protected String getMessage(ErrorCode error) {
		return messageSource.getMessage(error.getMessageKey(), null, error.getMessageKey(), null);
	}

	private String getMessage(ErrorCode error, Map<String, Object> properties) {
		String message = messageSource.getMessage(error.getMessageKey(), null, error.getMessageKey(), null);

		MessageFormat msgFmt = new MessageFormat(message);

		return msgFmt.format(properties);
	}

	protected Error getError(ErrorCode error) {
		return new Error(error.getMessageKey(), this.getMessage(error));
	}

	protected Error getError(ErrorCode error, Map<String, Object> properties) {
		return new Error(error.getMessageKey(), this.getMessage(error, properties));
	}

	private void validate(DataQuery query, DataQueryResponse response) {
		// Time
		if (query.getTime() == null) {
			response.add(this.getError(QueryErrorCode.TIME_FILTER_NOT_SET));
		} else {
			switch (query.getTime().getType()) {
				case ABSOLUTE:
					if (query.getTime().getEnd() == null) {
						response.add(this.getError(QueryErrorCode.TIME_FILTER_ABSOLUTE_END_NOT_SET));
					}
					break;
				case SLIDING:
					if (query.getTime().getDuration() == null) {
						response.add(this.getError(QueryErrorCode.TIME_FILTER_SLIDING_DURATION_NOT_SET));
					}
					break;
				default:
					response.add(this.getError(QueryErrorCode.TIME_FILTER_INVALID));
					break;
			}
		}

		// Spatial
		if (query.getSpatial() != null) {
			if (query.getSpatial().getGeometry() == null) {
				response.add(this.getError(QueryErrorCode.SPATIAL_FILTER_GEOMETRY_NOT_SET));
			}
			switch (query.getSpatial().getType()) {
				case CONTAINS:
					break;
				case INTERSECT:
					break;
				case DISTANCE:
					if (query.getSpatial().getDistance() == null) {
						response.add(this.getError(QueryErrorCode.SPATIAL_FILTER_DISTANCE_NOT_SET));
					}
					break;
				default:
					break;
			}
		}

		// Population
		if ((query.getPopulation() == null) || (query.getPopulation().size() == 0)) {
			response.add(this.getError(QueryErrorCode.POPULATION_FILTER_NOT_SET));
		} else {
			for (PopulationFilter filter : query.getPopulation()) {
				switch (filter.getType()) {
					case USER:
						UserPopulationFilter userFilter = (UserPopulationFilter) filter;
						if ((userFilter.getUsers() == null) || (userFilter.getUsers().size() == 0)) {
							response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
						}
						break;
					case GROUP:
						GroupPopulationFilter groupFilter = (GroupPopulationFilter) filter;
						if (groupFilter.getGroup() == null) {
							response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
						}
						break;
					case CLUSTER:
						ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;
						int propertyCount = 0;
						if (clusterFilter.getCluster() != null) {
							propertyCount++;
						}
						if (!StringUtils.isBlank(clusterFilter.getName())) {
							propertyCount++;
						}
						if ((clusterFilter.getClusterType() != null)
										&& (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
							propertyCount++;
						}
						if (propertyCount != 1) {
							response.add(this.getError(QueryErrorCode.POPULATION_FILTER_INVALID_CLUSTER));
						}
						break;
					case UTILITY:
						UtilityPopulationFilter utilityFilter = (UtilityPopulationFilter) filter;
						if (utilityFilter.getUtility() == null) {
							response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
						}
						break;
					default:
						response.add(this.getError(QueryErrorCode.POPULATION_FILTER_INVALID));
						break;

				}

				// Ranking
				if (filter.getRanking() != null) {
					if (filter.getRanking().getType().equals(EnumRankingType.UNDEFINED)) {
						response.add(this.getError(QueryErrorCode.RANKING_TYPE_NOT_SET));
					}
					if ((filter.getRanking().getLimit() == null) || (filter.getRanking().getLimit() < 1)) {
						response.add(this.getError(QueryErrorCode.RANKING_INVALID_LIMIT));
					}
					if (filter.getRanking().getField().equals(EnumDataField.UNDEFINED)) {
						response.add(this.getError(QueryErrorCode.RANKING_INVALID_FIELD));
					}
					if (filter.getRanking().getMetric().equals(EnumMetric.UNDEFINED)) {
						response.add(this.getError(QueryErrorCode.RANKING_INVALID_METRIC));
					}
				}
			}
		}

		// Metrics
		for (EnumMetric m : query.getMetrics()) {
			if (m.equals(EnumMetric.UNDEFINED)) {
				response.add(this.getError(QueryErrorCode.METRIC_INVALID));
			}
		}
	}

	@Override
	public DataQueryResponse execute(DataQuery query) {
		try {
			// Get authenticated user if any exists
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser authenticatedUser = null;

			if ((authentication != null) && (authentication.getPrincipal() instanceof AuthenticatedUser)) {
				authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
			}

			// If time zone is not set, use the current user's time zone
			DateTimeZone timezone = null;

			if (StringUtils.isBlank(query.getTimezone())) {
				if (authenticatedUser != null) {
					timezone = DateTimeZone.forID(authenticatedUser.getTimezone());
				} else {
					// If there is no authenticated user, user default UTC time
					// zone
					timezone = DateTimeZone.UTC;
				}
			} else {
				timezone = DateTimeZone.forID(query.getTimezone());
			}

			DataQueryResponse response = new DataQueryResponse(timezone);

			ExpandedDataQuery expandedQuery = new ExpandedDataQuery(timezone);

			// Validate query
			this.validate(query, response);
			if (!response.getSuccess()) {
				return response;
			}

			// Get all unique user keys for every group
			if ((query.getPopulation() != null) && (query.getPopulation().size() != 0)) {
				MessageDigest md = MessageDigest.getInstance("MD5");

				for (int p = 0; p < query.getPopulation().size(); p++) {
					PopulationFilter filter = query.getPopulation().get(p);

					List<UUID> filterUsers = null;
					switch (filter.getType()) {
						case USER:
							filterUsers = ((UserPopulationFilter) filter).getUsers();
							break;
						case GROUP:
							filterUsers = userRepository.getUserKeysForGroup(((GroupPopulationFilter) filter)
											.getGroup());
							break;
						case CLUSTER:
							ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;

							List<GroupCluster> groups = null;

							if (clusterFilter.getCluster() != null) {
								groups = userRepository.getClusterGroupByKey(clusterFilter.getCluster());
							} else if ((clusterFilter.getClusterType() != null)
											&& (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
								groups = userRepository.getClusterGroupByType(clusterFilter.getClusterType());
							} else if (!StringUtils.isBlank(clusterFilter.getName())) {
								groups = userRepository.getClusterGroupByName(clusterFilter.getName());
							}

							for (GroupCluster group : groups) {
								if (clusterFilter.getRanking() == null) {
									query.getPopulation().add(
													new GroupPopulationFilter(group.getName(), group.getKey()));
								} else {
									query.getPopulation().add(
													new GroupPopulationFilter(group.getName(), group.getKey(),
																	clusterFilter.getRanking()));
								}
							}
							continue;
						case UTILITY:
							filterUsers = userRepository.getUserKeysForUtility(((UtilityPopulationFilter) filter)
											.getUtility());
							break;
						default:
							// Ignore
					}

					// Construct expanded population filter
					ExpandedPopulationFilter expandedPopulationFilter;
					if (filter.getRanking() == null) {
						expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel());
					} else {
						expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel(), filter.getRanking());
					}

					if (filterUsers.size() > 0) {
						for (UUID userKey : filterUsers) {
							// Apply utility filter only when an authenticated
							// user exists
							AuthenticatedUser user = (authenticatedUser == null ? userRepository.getUserByKey(userKey)
											: userRepository.getUserByUtilityAndKey(authenticatedUser.getUtilityId(),
															userKey));

							if (user == null) {
								throw new ApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username",
												userKey);
							}

							// Load devices only if there is a spatial filter or
							// meter values are requested
							boolean includeUser = false;

							if ((query.getSpatial() != null)
											|| (query.getSource().equals(EnumMeasurementDataSource.BOTH))
											|| (query.getSource().equals(EnumMeasurementDataSource.METER))) {
								ArrayList<Device> devices = deviceRepository.getUserDevices(userKey,
												new DeviceRegistrationQuery(EnumDeviceType.METER));

								for (Device device : devices) {
									WaterMeterDevice meter = (WaterMeterDevice) device;

									if (query.getSpatial() == null) {
										includeUser = true;
										expandedPopulationFilter.getSerials().add(
														md.digest(meter.getSerial().getBytes("UTF-8")));
									} else {
										boolean includeMeter = false;

										if (meter.getLocation() != null) {
											switch (query.getSpatial().getType()) {
												case CONTAINS:
													includeMeter = query.getSpatial().getGeometry()
																	.contains(meter.getLocation());
													break;
												case INTERSECT:
													includeMeter = query.getSpatial().getGeometry()
																	.intersects(meter.getLocation());
													break;
												case DISTANCE:
													includeMeter = (query.getSpatial().getGeometry()
																	.distance(meter.getLocation()) < query.getSpatial()
																	.getDistance());
													break;
												default:
													// Ignore
											}
										}
										if (includeMeter) {
											includeUser = true;
											expandedPopulationFilter.getSerials().add(
															md.digest(meter.getSerial().getBytes("UTF-8")));
										}
									}
								}
							} else {
								includeUser = true;
							}

							// Decide if user should be added to the final
							// result
							if (includeUser) {
								expandedPopulationFilter.getUsers().add(userKey);
								expandedPopulationFilter.getLabels().add(user.getUsername());
								expandedPopulationFilter.getHashes().add(
												md.digest(userKey.toString().getBytes("UTF-8")));
							}

						}

						expandedQuery.getGroups().add(expandedPopulationFilter);
					}
				}
			}

			// Compute time constraints
			long startDateTime, endDateTime;

			startDateTime = query.getTime().getStart();

			switch (query.getTime().getType()) {
				case ABSOLUTE:
					endDateTime = query.getTime().getEnd();
					break;
				case SLIDING:
					switch (query.getTime().getDurationTimeUnit()) {
						case HOUR:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusHours(query.getTime()
											.getDuration()).getMillis());
							break;
						case DAY:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusDays(query.getTime()
											.getDuration()).getMillis());
							break;
						case WEEK:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusWeeks(query.getTime()
											.getDuration()).getMillis());
							break;
						case MONTH:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusMonths(query.getTime()
											.getDuration()).getMillis());
							break;
						case YEAR:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusYears(query.getTime()
											.getDuration()).getMillis());
							break;
						default:
							return response;
					}

					// Invert start/end dates if needed e.g. a negative interval
					// is selected for a sliding time window
					if (endDateTime < startDateTime) {
						long temp = startDateTime;
						startDateTime = endDateTime;
						endDateTime = temp;
					}
					break;
				default:
					return response;
			}

			// Construct expanded query
			expandedQuery.setStartDateTime(startDateTime);
			expandedQuery.setEndDateTime(endDateTime);
			expandedQuery.setGranularity(query.getTime().getGranularity());
			expandedQuery.setMetrics(query.getMetrics());

			switch (query.getSource()) {
				case BOTH:
					response.setDevices(amphiroRepository.query(expandedQuery));
					response.setMeters(meterRepository.query(expandedQuery));
					break;
				case AMPHIRO:
					response.setDevices(amphiroRepository.query(expandedQuery));
					break;
				case METER:
					response.setMeters(meterRepository.query(expandedQuery));
					break;
			}

			return response;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex);
		}
	}
}
