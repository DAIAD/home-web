package eu.daiad.web.service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryResponse;
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

			ExpandedDataQuery expandedQuery = new ExpandedDataQuery();

			// Get authenticated user
			AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext()
							.getAuthentication().getPrincipal();

			// At least one group or user must be selected. Time constraint is
			// required
			if ((query.getTime() == null) || (query.getPopulation() == null)) {
				return response;
			}

			// Get all unique user keys for every group
			if ((query.getPopulation() != null) && (query.getPopulation().size() != 0)) {
				MessageDigest md = MessageDigest.getInstance("MD5");

				for (PopulationFilter filter : query.getPopulation()) {

					ExpandedPopulationFilter expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel());

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

							// Apply spatial filtering
							if (query.getSpatial() != null) {
								ArrayList<Device> devices = deviceRepository.getUserDevices(userKey,
												new DeviceRegistrationQuery(EnumDeviceType.METER));

								for (Device device : devices) {
									WaterMeterDevice meter = (WaterMeterDevice) device;

									boolean include = false;

									if (meter.getLocation() != null) {
										switch (query.getSpatial().getType()) {
											case CONTAINS:
												include = query.getSpatial().getGeometry()
																.contains(meter.getLocation());
												break;
											case INTERSECT:
												include = query.getSpatial().getGeometry()
																.intersects(meter.getLocation());
												break;
											case DISTANCE:
												include = (query.getSpatial().getGeometry()
																.distance(meter.getLocation()) < query.getSpatial()
																.getDistance());
												break;
											default:
												// Ignore
										}
									}
									if (include) {
										expandedPopulationFilter.getUsers().add(userKey);
										expandedPopulationFilter.getHashes().add(
														md.digest(userKey.toString().getBytes("UTF-8")));
										break;
									}
								}
							} else {
								expandedPopulationFilter.getUsers().add(userKey);
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
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).minusHours(query.getTime()
											.getDuration()).getMillis());
							break;
						case DAY:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).minusDays(query.getTime()
											.getDuration()).getMillis());
							break;
						case WEEK:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).minusWeeks(query.getTime()
											.getDuration()).getMillis());
							break;
						case MONTH:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).minusMonths(query.getTime()
											.getDuration()).getMillis());
							break;
						case YEAR:
							endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).minusYears(query.getTime()
											.getDuration()).getMillis());
							break;
						default:
							return response;
					}
					break;
				default:
					return response;
			}

			// Construct expanded query
			expandedQuery.setStartDateTime(startDateTime);
			expandedQuery.setEndDateTime(endDateTime);
			expandedQuery.setGranularity(query.getTime().getGraunlarity());
			expandedQuery.setMetrics(query.getMetrics());

			response.setDevices(amphiroRepository.query(expandedQuery));

			return response;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex);
		}
	}
}
