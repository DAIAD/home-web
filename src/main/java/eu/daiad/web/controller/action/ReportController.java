package eu.daiad.web.controller.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ResourceNotFoundException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.report.ReportStatusResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.IReportRepository;
import eu.daiad.web.repository.application.IUserRepository;

/**
 * Provides actions for loading DAIAD reports.
 */
@RestController
public class ReportController extends BaseController {

    /**
     * Base application URL
     */
    @Value("${daiad.url}")
    private String baseUrl;

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ReportController.class);

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing report data.
     */
    @Autowired
    private IReportRepository reportRepository;

    /**
     * Downloads a report for the authenticated user.
     *
     * @param authenticatedUser the currently authenticated user.
     * @param year the report year.
     * @param month the report month.
     * @return the report file.
     */
    @RequestMapping(value = "/action/report/download/{year}/{month}", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_USER })
    public ResponseEntity<InputStreamResource> downloadReport(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                              @PathVariable int year,
                                                              @PathVariable int month) {
        String path = reportRepository.getReportPath(authenticatedUser.getUsername(), year, month);

        String filename = String.format("%s_%d_%02d.pdf", authenticatedUser.getUsername(), year, month);

        return sendReportFile(path, filename, MediaType.APPLICATION_PDF);
    }

    /**
     * Checks if a report for the authenticated user exists.
     *
     * @param authenticatedUser the currently authenticated user.
     * @param year the report year.
     * @param month the report month.
     * @return {@code true} if the report exists.
     */
    @RequestMapping(value = "/action/report/status/{year}/{month}", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse checkReport(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                    @PathVariable int year,
                                    @PathVariable int month) {
        String path = reportRepository.getReportPath(authenticatedUser.getUsername(), year, month);

        return createReportStatus(path, String.format("action/report/download/%d/%d", year, month));
    }

    /**
     * Downloads a report for the selected user.
     *
     * @param authenticatedUser the currently authenticated user.
     * @param userKey the key of the user for which the report is requested.
     * @param year the report year.
     * @param month the report month.
     * @return the report file.
     */
    @RequestMapping(value = "/action/report/download/{userKey}/{year}/{month}", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public ResponseEntity<InputStreamResource> downloadExportedDataFile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                                        @PathVariable UUID userKey,
                                                                        @PathVariable int year,
                                                                        @PathVariable int month) {
        AuthenticatedUser reportOwner = userRepository.getUserByKey(userKey);
        if (!authenticatedUser.getUtilities().contains(reportOwner.getUtilityId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        String path = reportRepository.getReportPath(reportOwner.getUsername(), year, month);

        String filename = String.format("%s_%d_%02d.pdf", reportOwner.getUsername(), year, month);

        return sendReportFile(path, filename, MediaType.APPLICATION_PDF);
    }

    /**
     * Checks if a report for a selected user exists.
     *
     * @param authenticatedUser the currently authenticated user.
     * @param userKey the key of the user for which the report is requested.
     * @param year the report year.
     * @param month the report month.
     * @return the report file.
     */
    @RequestMapping(value = "/action/report/status/{userKey}/{year}/{month}", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse checkReport(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                    @PathVariable UUID userKey,
                                    @PathVariable int year,
                                    @PathVariable int month) {
        AuthenticatedUser reportOwner = userRepository.getUserByKey(userKey);
        if (!authenticatedUser.getUtilities().contains(reportOwner.getUtilityId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        String path = reportRepository.getReportPath(reportOwner.getUsername(), year, month);

        return createReportStatus(path, String.format("action/report/download/%s/%d/%d", userKey.toString(), year, month));
    }

    /**
     * Creates a response for downloading a file.
     *
     * @param path the file location.
     * @param filename the download file name.
     * @param type file media type.
     * @return an instance of {@link ResponseEntity}.
     */
    private ResponseEntity<InputStreamResource> sendReportFile(String path, String filename, MediaType type) {
        try {
            File file = new File(path);

            if (file.exists()) {
                FileSystemResource fileResource = new FileSystemResource(file);

                return ResponseEntity.ok()
                                     .headers(getDownloadResponseHeaders(filename))
                                     .contentLength(fileResource.contentLength())
                                     .contentType(type)
                                     .body(new InputStreamResource(fileResource.getInputStream()));
            } else {
                logger.warn(String.format("File [%s] does not exist.", Paths.get(path)));
            }
        } catch(IOException ex) {
            logger.error(String.format("Failed to download file [%s].", Paths.get(path)), ex);
        }

        throw new ResourceNotFoundException();
    }

    /**
     * Checks if a report exists.
     *
     * @param path the file location.
     * @param url the URL for downloading the report.
     * @return an instance of {@link RestResponse}.
     */
    private RestResponse createReportStatus(String path, String url) {
        try {
            File file = new File(path);

            if (file.exists()) {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(),  BasicFileAttributes.class);

                return new ReportStatusResponse(new DateTime(attr.creationTime().toMillis()),
                                                file.length(),
                                                baseUrl.endsWith("/") ? baseUrl + url : baseUrl + "/" + url);
            } else {
                return this.createResponse(SharedErrorCode.RESOURCE_NOT_FOUND);
            }
        } catch(IOException ex) {
            logger.error(String.format("Failed to query report metadata for file [%s].", Paths.get(path)), ex);
        }

        return this.createResponse(SharedErrorCode.UNKNOWN);
    }

    /**
     * Creates HTTP headers for downloading a file.
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

}
