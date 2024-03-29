package eu.daiad.api.controller.api;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.api.controller.BaseRestController;
import eu.daiad.common.model.DeviceMeasurementCollection;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.common.model.amphiro.HistoricalToRealTimeRequest;
import eu.daiad.common.model.amphiro.IgnoreShowerRequest;
import eu.daiad.common.model.amphiro.MemberAssignmentRequest;
import eu.daiad.common.model.device.AmphiroDevice;
import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.error.DeviceErrorCode;
import eu.daiad.common.model.error.QueryErrorCode;
import eu.daiad.common.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryRequest;
import eu.daiad.common.model.query.ForecastQuery;
import eu.daiad.common.model.query.ForecastQueryRequest;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.common.repository.application.IAmphiroTimeOrderedRepository;
import eu.daiad.common.repository.application.IDeviceRepository;
import eu.daiad.common.repository.application.IMeterDataRepository;
import eu.daiad.common.service.IDataService;

/**
 * Provides actions for storing Amphiro B1 data to the server and querying
 * stored data.
 */
@RestController
public class DataController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DataController.class);

    /**
     * Folder for storing temporary files.
     */
    @Value("${tmp.folder}")
    private String temporaryPath;

    /**
     * Repository for accessing amphiro b1 data indexed by time.
     */
    @Autowired
    private IAmphiroTimeOrderedRepository amphiroTimeOrderedRepository;

    /**
     * Repository for accessing amphiro b1 data indexed by shower id.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    /**
     * Repository for accessing smart water meter data.
     */
    @Autowired
    private IMeterDataRepository waterMeterMeasurementRepository;

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Service for querying amphiro b1 and smart water meter data stored in HBase.
     */
    @Autowired
    private IDataService dataService;

    /**
     * General purpose method for querying data using a set of filtering
     * criteria. Depending on the given criteria, more than one data series may
     * be returned.
     *
     * @param data the query.
     * @return the data series.
     */
    @PostMapping(value = "/api/v1/data/query")
    public RestResponse query(@RequestBody DataQueryRequest data) {
        try {
            AuthenticatedUser user = authenticate(data.getCredentials(), EnumRole.ROLE_USER, EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            DataQuery query = data.getQuery();
            if (query == null) {
                return createResponse(QueryErrorCode.EMPTY_QUERY);
            }

            if(StringUtils.isBlank(query.getTimezone())) {
                query.setTimezone(user.getTimezone());
            }

            return dataService.execute(query);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns forecasting results for a smart water meter
     *
     * @param data the query.
     * @return the data series.
     */
    @PostMapping(value = "/api/v1/data/meter/forecast")
    public RestResponse forecast(@RequestBody ForecastQueryRequest data) {
        try {
            AuthenticatedUser user = authenticate(data.getCredentials(), EnumRole.ROLE_USER, EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            ForecastQuery query = data.getQuery();
            if (query == null) {
                return createResponse(QueryErrorCode.EMPTY_QUERY);
            }

            if (StringUtils.isBlank(query.getTimezone())) {
                query.setTimezone(user.getTimezone());
            }

            return dataService.execute(query);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Stores Amphiro B1 session and measurement data. Sessions are index by
     * time.
     *
     * @param data the data to store.
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v1/data/store")
    public RestResponse storeUsingAmphiroTimeOrdering(@RequestBody DeviceMeasurementCollection data) {
        RestResponse response = new RestResponse();

        AuthenticatedUser authenticatedUser = null;
        Device device = null;

        boolean success = true;

        try {
            switch (data.getType()) {
                case AMPHIRO:
                    if (data instanceof AmphiroMeasurementCollection) {
                        authenticatedUser = authenticate(data.getCredentials(), EnumRole.ROLE_USER);

                        device = deviceRepository.getUserDeviceByKey(authenticatedUser.getKey(), data
                                        .getDeviceKey());

                        if (device == null) {
                            throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
                                            data.getDeviceKey().toString());
                        }

                        if (!device.getType().equals(EnumDeviceType.AMPHIRO)) {
                            throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
                                            data.getType().toString());
                        }

                        amphiroTimeOrderedRepository.storeData(authenticatedUser, (AmphiroDevice) device, (AmphiroMeasurementCollection) data);
                    }
                    break;
                case METER:
                    if (data instanceof WaterMeterMeasurementCollection) {
                        authenticatedUser = authenticate(data.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

                        device = deviceRepository.getDeviceByKey(data.getDeviceKey());

                        if (device == null) {
                            throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
                                            data.getDeviceKey().toString());
                        }

                        if (!device.getType().equals(EnumDeviceType.METER)) {
                            throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
                                            data.getType().toString());
                        }

                        waterMeterMeasurementRepository.store(((WaterMeterDevice) device).getSerial(), (WaterMeterMeasurementCollection) data);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));

            success = false;
        } finally {
            logDataUploadSession(authenticatedUser, device, success);
        }

        return response;
    }

    /**
     * Stores Amphiro B1 session and measurement data. Sessions are index by id.
     *
     * @param data the data to store
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v2/data/store")
    public RestResponse storeUsingAmphiroIndexOredering(@RequestBody DeviceMeasurementCollection data) {
        RestResponse response = new RestResponse();

        AuthenticatedUser authenticatedUser = null;
        Device device = null;

        boolean success = true;

        try {
            switch (data.getType()) {
                case AMPHIRO:
                    if (data instanceof AmphiroMeasurementCollection) {
                        authenticatedUser = authenticate(data.getCredentials(), EnumRole.ROLE_USER);

                        device = deviceRepository.getUserDeviceByKey(authenticatedUser.getKey(), data
                                        .getDeviceKey());

                        if (device == null) {
                            throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
                                            data.getDeviceKey().toString());
                        }

                        if (!device.getType().equals(EnumDeviceType.AMPHIRO)) {
                            throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
                                            data.getType().toString());
                        }
                        response = amphiroIndexOrderedRepository.store(authenticatedUser, (AmphiroDevice) device, (AmphiroMeasurementCollection) data);
                    }
                    break;
                case METER:
                    if (data instanceof WaterMeterMeasurementCollection) {
                        authenticatedUser = authenticate(data.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

                        device = deviceRepository.getDeviceByKey(data.getDeviceKey());

                        if (device == null) {
                            throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
                                            data.getDeviceKey().toString());
                        }

                        if (!device.getType().equals(EnumDeviceType.METER)) {
                            throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
                                            data.getType().toString());
                        }

                        waterMeterMeasurementRepository.store(((WaterMeterDevice) device).getSerial(), (WaterMeterMeasurementCollection) data);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));

            success = false;
        } finally {
            logDataUploadSession(authenticatedUser, device, success);
        }

        return response;
    }

    /**
     * Assigns household members to amphiro b1 sessions.
     *
     * @param request member assignment data
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v2/data/session/member")
    public RestResponse assignMemberToSession(@RequestBody MemberAssignmentRequest request) {
        RestResponse response = new RestResponse();

        AuthenticatedUser authenticatedUser = null;

        try {
            authenticatedUser = authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            amphiroIndexOrderedRepository.assignMember(authenticatedUser, request.getAssignments());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Marks an amphiro b1 message as not being a shower.
     *
     * @param request shower data.
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v2/data/session/ignore")
    public RestResponse invalidateSession(@RequestBody IgnoreShowerRequest request) {
        RestResponse response = new RestResponse();

        AuthenticatedUser authenticatedUser = null;

        try {
            authenticatedUser = authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            amphiroIndexOrderedRepository.ignore(authenticatedUser, request.getSessions());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Updates the date time of a historical shower and converts it to a real-time one.
     *
     * @param request the shower data including its unique id and timestamp.
     * @return an instance of {@link RestResponse}.
     */
    @PostMapping(value = "/api/v2/data/session/date")
    public RestResponse convertHistoricalToRealTimeShower(@RequestBody HistoricalToRealTimeRequest request) {
        RestResponse response = new RestResponse();

        AuthenticatedUser authenticatedUser = null;

        try {
            authenticatedUser = authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            AmphiroDevice device = deviceRepository.getUserAmphiroByKey(authenticatedUser.getKey(), request.getDeviceKey());
            if(device == null) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", request.getDeviceKey());
            }
            amphiroIndexOrderedRepository.toRealTime(authenticatedUser, device, request.getSessionId(), request.getTimestamp());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Logs the last data upload operation for a device.
     *
     * @param user the currently authenticated user.
     * @param device the device that uploaded data.
     * @param success if the operation was successful.
     */
    private void logDataUploadSession(AuthenticatedUser user, Device device, boolean success) {
        try {
            if ((user == null) || (device == null)) {
                return;
            }
            deviceRepository.setLastDataUploadDate(user.getKey(), device.getKey(), new DateTime(), success);
        } catch (Exception ex) {
            // Ignore exceptions
        }
    }
    
}
