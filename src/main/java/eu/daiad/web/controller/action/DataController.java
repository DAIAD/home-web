package eu.daiad.web.controller.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.HistoricalToRealTimeRequest;
import eu.daiad.web.model.amphiro.IgnoreShowerRequest;
import eu.daiad.web.model.amphiro.MemberAssignmentRequest;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.error.ActionErrorCode;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.error.QueryErrorCode;
import eu.daiad.web.model.error.ResourceNotFoundException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.export.DataExportFileQuery;
import eu.daiad.web.model.export.DataExportFileQueryResult;
import eu.daiad.web.model.export.DataExportFileRequest;
import eu.daiad.web.model.export.DataExportFileResponse;
import eu.daiad.web.model.export.DownloadFileResponse;
import eu.daiad.web.model.export.ExportFile;
import eu.daiad.web.model.export.UserDataExportRequest;
import eu.daiad.web.model.loader.EnumUploadFileType;
import eu.daiad.web.model.loader.ImportWaterMeterFileConfiguration;
import eu.daiad.web.model.loader.UploadRequest;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryCollectionResponse;
import eu.daiad.web.model.query.DataQueryRequest;
import eu.daiad.web.model.query.ForecastQuery;
import eu.daiad.web.model.query.ForecastQueryRequest;
import eu.daiad.web.model.query.StoreDataQueryRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.model.spatial.ReferenceSystem;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IExportRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataImportService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.IWaterMeterDataLoaderService;
import eu.daiad.web.service.etl.IDataExportService;
import eu.daiad.web.service.etl.UserDataExportQuery;

/**
 * Provides methods for managing, querying and exporting data.
 */
@RestController
public class DataController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DataController.class);

    /**
     * Media type for ZIP archive file format.
     */
    private final static MediaType APPLICATION_ZIP = MediaType.parseMediaType("application/zip");

    /**
     * Folder where temporary files are saved.
     */
    @Value("${tmp.folder}")
    private String workingDirectory;

    /**
     * Service for exporting data.
     */
    @Autowired
    private IDataExportService exportService;

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
     * Service for importing data.
     */
    @Autowired
    private IDataImportService fileDataLoaderService;

    /**
     * Service for importing smart water meter data.
     */
    @Autowired
    private IWaterMeterDataLoaderService waterMeterDataLoaderService;

    /**
     * Repository for accessing amphiro b1 data indexed by shower id.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    /**
     * Service for querying smart water and amphiro b1 data in HBASE.
     */
    @Autowired
    private IDataService dataService;

    /**
     * Repository for accessing exported data files.
     */
    @Autowired
    private IExportRepository exportRepository;

    /**
     * Uploads a data file to the server and perform an action on it e.g. import smart water meter data, assign
     * accounts to smart water meters etc.
     *
     * @param request the upload file and action.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/upload", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse upload(UploadRequest request) {
        try {
            if (request.getFiles() != null) {
                // Create working directory if not already exists
                FileUtils.forceMkdir(new File(workingDirectory));

                switch (request.getType()) {
                    case METER:
                        // Check SRID
                        Integer srid = request.getSrid();

                        if (srid == null) {
                            return new RestResponse(SharedErrorCode.INVALID_SRID, this.getMessage(SharedErrorCode.INVALID_SRID));
                        }

                        for (MultipartFile file : request.getFiles()) {
                            String filename = createTemporaryFilename(file.getBytes());

                            ImportWaterMeterFileConfiguration configuration = new ImportWaterMeterFileConfiguration(filename, file.getOriginalFilename());

                            configuration.setSourceReferenceSystem(new ReferenceSystem(request.getSrid()));
                            configuration.setFirstRowHeader(request.isFirstRowHeader());

                            fileDataLoaderService.importWaterMeter(configuration);
                        }
                        break;
                    case METER_DATA:
                        // Check time zone
                        validateTimezone(request.getTimezone());

                        for (MultipartFile file : request.getFiles()) {
                            String filename = createTemporaryFilename(file.getBytes());

                            waterMeterDataLoaderService.parse(filename, request.getTimezone(), EnumUploadFileType.METER_DATA, null);
                        }
                        break;
                    case METER_DATA_FORECAST:
                        // Check time zone
                        validateTimezone(request.getTimezone());

                        for (MultipartFile file : request.getFiles()) {
                            String filename = createTemporaryFilename(file.getBytes());

                            waterMeterDataLoaderService.parse(filename, request.getTimezone(), EnumUploadFileType.METER_DATA_FORECAST, null);
                        }
                        break;
                    default:
                        throw ApplicationException.create(SharedErrorCode.NOT_IMPLEMENTED, getMessage(SharedErrorCode.NOT_IMPLEMENTED));
                }
            }
        } catch (ApplicationException ex) {
            return new RestResponse(ex.getCode(), getMessage(ex));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return createResponse(SharedErrorCode.UNKNOWN);
        }

        return new RestResponse();
    }

    /**
     * Query amphiro b1 session data and smart water meter readings using one or more filtering
     * criteria. Depending on the search criteria, one or more data series may be returned.
     *
     * @param user the currently authenticated user.
     * @param data the data query.
     * @return the data series.
     */
    @RequestMapping(value = "/action/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse query(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody DataQueryRequest data) {
        try {
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
     * Saves a data query.
     *
     * @param user the user
     * @param request the data.
     * @return the result of the save operation.
     */
    @RequestMapping(value = "/action/data/query/store", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse storeQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {
        try {

            List<DataQuery> queries = request.getNamedQuery().getQueries();

            if (queries == null || queries.isEmpty()) {
                return createResponse(QueryErrorCode.EMPTY_QUERY);
            }

            if (StringUtils.isBlank(queries.get(0).getTimezone())) {
                queries.get(0).setTimezone(user.getTimezone());
            }

            dataService.storeQuery(request.getNamedQuery(), user.getKey());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Saves a data query.
     *
     * @param user the user
     * @param request the data.
     * @return the result of the save operation.
     */
    @RequestMapping(value = "/action/data/query/update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse updateStoredQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {
        RestResponse response = new RestResponse();

        try {

            // Set defaults if needed
            List<DataQuery> queries = request.getNamedQuery().getQueries();
            if (queries != null && !queries.isEmpty()) {
                // Initialize time zone
                if (StringUtils.isBlank(queries.get(0).getTimezone())) {
                    queries.get(0).setTimezone(user.getTimezone());
                }
            }

            dataService.updateStoredQuery(request.getNamedQuery(), user.getKey());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Deletes a data query.
     *
     * @param user the user
     * @param request the data.
     * @return the result of the delete operation.
     */
    @RequestMapping(value = "/action/data/query/delete", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse deleteStoredQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {
        RestResponse response = new RestResponse();

        try {

            dataService.deleteStoredQuery(request.getNamedQuery(), user.getKey());

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Pin query to dashboard.
     *
     * @param user the user
     * @param request the requested query to pin.
     * @return the result of the operation.
     */
    @RequestMapping(value = "/action/data/query/pin", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse pinQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {
        RestResponse response = new RestResponse();

        try {

            dataService.pinStoredQuery(request.getNamedQuery().getId(), user.getKey());

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Unpin query from dashboard.
     *
     * @param user the user
     * @param request the requested query to unpin.
     * @return the result of the operation.
     */
    @RequestMapping(value = "/action/data/query/unpin", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse unpinQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {

        RestResponse response = new RestResponse();

        try {

            dataService.unpinStoredQuery(request.getNamedQuery().getId(), user.getKey());

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Loads all saved data queries.
     *
     * @param user the user
     * @return the saved data queries
     */
    @RequestMapping(value = "/action/data/query/load", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getAllQueries(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            DataQueryCollectionResponse response = new DataQueryCollectionResponse();

            response.setQueries(dataService.getQueriesForOwner(user.getId()));

            return response;
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
    @RequestMapping(value = "/action/data/meter/forecast", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse forecast(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody ForecastQueryRequest data) {
        try {
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
     * Exports amphiro b1 sessions and smart water meter data for a single user based on a query.
     *
     * @param user the currently authenticated user.
     * @param data the query for selecting the data to export.
     * @return a token for downloading the generated file.
     */
    @RequestMapping(value = "/action/data/export", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse export(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody UserDataExportRequest data) {
        try {
            switch (data.getType()) {
                case USER:
                    UserDataExportQuery exportQuery = new UserDataExportQuery();

                    // Get account for which data export is requested
                    AuthenticatedUser exportedUser = userRepository.getUserByKey(data.getUserKey());
                    // User must exists
                    if(exportedUser == null) {
                        throw createApplicationException(UserErrorCode.USER_KEY_NOT_FOUND);
                    }
                    // The request authenticated user must have access to the user utility data
                    if ((!user.getKey().equals(exportedUser.getKey())) &&
                        (!user.getUtilities().contains(exportedUser.getUtilityId()))) {
                        throw createApplicationException(SharedErrorCode.AUTHORIZATION);
                    }

                    exportQuery.setUserKey(exportedUser.getKey());
                    exportQuery.setDeviceKeys(data.getDeviceKeys());

                    exportQuery.setStartTimstamp(data.getStartDateTime());
                    exportQuery.setEndTimestamp(data.getEndDateTime());

                    if (StringUtils.isBlank(data.getTimezone())) {
                        exportQuery.setTimezone(exportedUser.getTimezone());
                    } else {
                        exportQuery.setTimezone(data.getTimezone());
                    }

                    return new DownloadFileResponse(exportService.export(exportQuery));
                default:
                    throw createApplicationException(ActionErrorCode.EXPORT_TYPE_NOT_SUPPORTED).set("type", data.getType());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(this.getError(ex));
        }
    }

    /**
     * Downloads a file that contains exported user data based on a unique token.
     *
     * @param token the token used to identify the file to download.
     * @return the file.
     */
    @RequestMapping(value = "/action/data/download/{token}", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public ResponseEntity<InputStreamResource> download(@PathVariable("token") String token) {
        try {
            File path = new File(workingDirectory);

            File file = new File(path, token + ".zip");

            if (file.exists()) {
                FileSystemResource fileResource = new FileSystemResource(file);

                return ResponseEntity.ok()
                                     .headers(getDownloadResponseHeaders("user-export-data.zip"))
                                     .contentLength(fileResource.contentLength())
                                     .contentType(APPLICATION_ZIP)
                                     .body(new InputStreamResource(fileResource.getInputStream()));
            }
        } catch (Exception ex) {
            logger.error(String.format("File [%s] was not found.", token), ex);
        }

        throw new ResourceNotFoundException();
    }

    /**
     * Create HTTP headers for downloading file.
     *
     * @param filename optional file name
     * @return the response headers.
     */
    private HttpHeaders getDownloadResponseHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();

        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        if(!StringUtils.isBlank(filename)) {
            headers.add("content-disposition", "attachment; filename=\"" + filename +"\"");
        }

        return headers;
    }

    /**
     * Creates a new unique filename given an initial filename and stores the given array of bytes.
     *
     * @param data the content to write to the file
     * @return a unique filename.
     * @throws IOException in case of an I/O error
     */
    private String createTemporaryFilename(byte[] data) throws IOException {
        String filename =  Paths.get(workingDirectory, UUID.randomUUID().toString()).toString();

        FileUtils.writeByteArrayToFile(new File(filename),  data);

        return filename;
    }

    /**
     * Validates a given time zone.
     *
     * @param timezone the time zone to validate.
     * @throws IOException in case the time zone is missing or is not valid.
     */
    private void validateTimezone(String timezone) throws ApplicationException {
        Set<String> zones = DateTimeZone.getAvailableIDs();

        if (StringUtils.isBlank(timezone)) {
            throw ApplicationException.create(SharedErrorCode.INVALID_TIME_ZONE, getMessage(SharedErrorCode.INVALID_TIME_ZONE));
        } else if (!zones.contains(timezone)) {
            throw ApplicationException.create(SharedErrorCode.TIMEZONE_NOT_FOUND, getMessage(SharedErrorCode.INVALID_TIME_ZONE))
                                      .set("timezone", timezone);
        }
    }

    /**
     * Returns all the exported data files. The operation supports pagination.
     *
     * @param user the currently authenticated user.
     * @param data the data query.
     * @return the response with the exported data files.
     */
    @RequestMapping(value = "/action/export/files", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getExportDataFiles(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody DataExportFileRequest data) {
        try {
            // Initialize and configure query
            DataExportFileQuery query = data.getQuery();
            if (query == null) {
                query = new DataExportFileQuery();
            }
            query.setUtilities(user.getUtilities());
            query.setDays(30);

            if (query.getIndex() < 0) {
                query.setIndex(0);
            }
            if (query.getSize() < 1) {
                query.setSize(10);
            }

            DataExportFileQueryResult result = exportRepository.getValidExportFiles(query);

            DataExportFileResponse response = new DataExportFileResponse();
            response.setIndex(query.getIndex());
            response.setSize(query.getSize());
            response.setFiles(result.getFiles());
            response.setTotal(result.getTotal());

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Downloads a file that contains exported user data based on a unique token.
     *
     * @param user the currently authenticated user.
     * @param token the token used to identify the file to download.
     * @return the file.
     */
    @RequestMapping(value = "/action/export/download/{token}", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public ResponseEntity<InputStreamResource> downloadExportedDataFile(@AuthenticationPrincipal AuthenticatedUser user,
                                                                        @PathVariable("token") String token) {
        try {
            UUID key = UUID.fromString(token);

            ExportFile exportFile = exportRepository.getExportFileByKey(key);

            File file = new File(FilenameUtils.concat(exportFile.getPath(), exportFile.getFilename()));

            if(file.exists() && (user.getUtilities().contains(exportFile.getUtilityId()))) {
                FileSystemResource fileResource = new FileSystemResource(file);

                return ResponseEntity.ok()
                                     .headers(getDownloadResponseHeaders(exportFile.getFilename()))
                                     .contentLength(fileResource.contentLength())
                                     .contentType(APPLICATION_ZIP)
                                     .body(new InputStreamResource(fileResource.getInputStream()));
            }
        } catch (Exception ex) {
            logger.error(String.format("File [%s] was not found.", token), ex);
        }

        throw new ResourceNotFoundException();
    }

    /**
     * Assigns household members to amphiro b1 sessions.
     *
     * @param user the currently authenticated user.
     * @param request member assignment data
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/data/session/member", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse assignMemberToSession(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody MemberAssignmentRequest request) {
        RestResponse response = new RestResponse();

        try {
            amphiroIndexOrderedRepository.assignMember(user, request.getAssignments());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Marks an amphiro b1 message as not being a shower.
     *
     * @param user the currently authenticated user.
     * @param request shower data.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/data/session/ignore", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse invalidateShower(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody IgnoreShowerRequest request) {
        RestResponse response = new RestResponse();

        try {
            amphiroIndexOrderedRepository.ignore(user, request.getSessions());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Updates the date time of a historical shower and converts it to a real-time one.
     *
     * @param user the currently authenticated user.
     * @param data the shower data including its unique id and timestamp.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/data/session/date", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse convertHistoricalToRealTimeShower(@AuthenticationPrincipal AuthenticatedUser user,
                                                          @RequestBody HistoricalToRealTimeRequest request) {
        RestResponse response = new RestResponse();

        try {
            AmphiroDevice device = deviceRepository.getUserAmphiroByKey(user.getKey(), request.getDeviceKey());
            if(device == null) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", request.getDeviceKey());
            }
            amphiroIndexOrderedRepository.toRealTime(user, device, request.getSessionId(), request.getTimestamp());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }
}
