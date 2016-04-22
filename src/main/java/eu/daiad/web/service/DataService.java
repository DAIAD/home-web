package eu.daiad.web.service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.EnumRankingType;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.GroupPopulationFilter;
import eu.daiad.web.model.query.PopulationFilter;
import eu.daiad.web.model.query.UserPopulationFilter;
import eu.daiad.web.model.query.UtilityPopulationFilter;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAmphiroMeasurementRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

@Service
public class DataService implements IDataService {

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	IAmphiroMeasurementRepository amphiroRepository;

	@Autowired
	IWaterMeterMeasurementRepository meterRepository;

	@Override
	public DataQueryResponse execute(DataQuery query) {
		try {
			DataQueryResponse response = new DataQueryResponse();

			// Get authenticated user
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser authenticatedUser = null;

			if (auth.getPrincipal() instanceof AuthenticatedUser) {
				authenticatedUser = (AuthenticatedUser) auth.getPrincipal();
			} else {
				throw new ApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
			}

			ExpandedDataQuery expandedQuery = new ExpandedDataQuery(DateTimeZone.forID(authenticatedUser.getTimezone()));

			// At least one group or user must be selected. Time constraint is
			// required
			if ((query.getTime() == null) || (query.getPopulation() == null)) {
				return response;
			}

			// Get all unique user keys for every group
			if ((query.getPopulation() != null) && (query.getPopulation().size() != 0)) {
				MessageDigest md = MessageDigest.getInstance("MD5");

				for (PopulationFilter filter : query.getPopulation()) {
					// Initialize ranking settings
					if (filter.getRanking() != null) {
						if (filter.getRanking().getType().equals(EnumRankingType.UNDEFINED)) {
							filter.getRanking().setType(EnumRankingType.TOP);
						}
						if ((filter.getRanking().getLimit() == null) || (filter.getRanking().getLimit() < 1)) {
							filter.getRanking().setLimit(1);
						}
					}

					// Construct expanded population filter
					ExpandedPopulationFilter expandedPopulationFilter;
					if (filter.getRanking() == null) {
						expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel());
					} else {
						expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel(), filter.getRanking());
					}

					ArrayList<UUID> filterUsers = null;
					switch (filter.getType()) {
						case USER:
							filterUsers = ((UserPopulationFilter) filter).getUsers();
							break;
						case GROUP:
							filterUsers = userRepository.getUserKeysForGroup(((GroupPopulationFilter) filter)
											.getGroup());
							break;
						case UTILITY:
							filterUsers = userRepository.getUserKeysForUtility(((UtilityPopulationFilter) filter)
											.getUtility());
							break;
						default:
							// Ignore
					}

					if (filterUsers.size() > 0) {
						for (UUID userKey : filterUsers) {
							AuthenticatedUser user = userRepository.getUserByUtilityAndKey(
											authenticatedUser.getUtilityId(), userKey);

							if (user == null) {
								throw new ApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username",
												userKey);
							}

							// Load devices only if there is a spatial filter or
							// meter values are requested
							if ((query.getSpatial() != null)
											|| (query.getSource().equals(EnumMeasurementDataSource.BOTH))
											|| (query.getSource().equals(EnumMeasurementDataSource.METER))) {
								ArrayList<Device> devices = deviceRepository.getUserDevices(userKey,
												new DeviceRegistrationQuery(EnumDeviceType.METER));

								boolean includeUser = false;

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
								if (includeUser) {
									expandedPopulationFilter.getUsers().add(userKey);
									if (filter.getRanking() != null) {
										expandedPopulationFilter.getLabels().add(user.getUsername());
									}
									expandedPopulationFilter.getHashes().add(
													md.digest(userKey.toString().getBytes("UTF-8")));
								}
							} else {
								expandedPopulationFilter.getUsers().add(userKey);
								if (filter.getRanking() != null) {
									expandedPopulationFilter.getLabels().add(user.getUsername());
								}
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
