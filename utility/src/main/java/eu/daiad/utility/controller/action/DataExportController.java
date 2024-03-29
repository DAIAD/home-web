package eu.daiad.utility.controller.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ActionErrorCode;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.ResourceNotFoundException;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.error.UserErrorCode;
import eu.daiad.common.model.export.DataExportFileQuery;
import eu.daiad.common.model.export.DataExportFileQueryResult;
import eu.daiad.common.model.export.DataExportFileRequest;
import eu.daiad.common.model.export.DataExportFileResponse;
import eu.daiad.common.model.export.DownloadFileResponse;
import eu.daiad.common.model.export.ExportFile;
import eu.daiad.common.model.export.UserDataExportRequest;
import eu.daiad.common.model.loader.EnumUploadFileType;
import eu.daiad.common.model.loader.ImportWaterMeterFileConfiguration;
import eu.daiad.common.model.loader.UploadRequest;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.RoleConstant;
import eu.daiad.common.model.spatial.ReferenceSystem;
import eu.daiad.common.repository.application.IExportRepository;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.service.IWaterMeterDataLoaderService;
import eu.daiad.utility.controller.BaseController;
import eu.daiad.utility.service.IDataImportService;
import eu.daiad.utility.service.etl.IDataExportService;
import eu.daiad.utility.service.etl.UserDataExportQuery;

/**
 * Provides methods for managing, querying and exporting data.
 */
@RestController
public class DataExportController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DataExportController.class);

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

                    exportQuery.setStartTimestamp(data.getStartDateTime());
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
    public ResponseEntity<InputStreamResource> downloadUserExportedDataFile(@PathVariable("token") String token) {
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

            if ((query.getIndex() != null) && (query.getIndex() < 0)) {
                query.setIndex(0);
            }
            if ((query.getSize() != null) && (query.getSize() < 1)) {
                query.setSize(10);
            }

            DataExportFileQueryResult result = exportRepository.getNotExpiredExportFiles(query);

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
    public ResponseEntity<InputStreamResource> downloadUtilityExportedDataFile(@AuthenticationPrincipal AuthenticatedUser user,
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

}
