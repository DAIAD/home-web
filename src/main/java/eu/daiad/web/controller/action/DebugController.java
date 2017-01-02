package eu.daiad.web.controller.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.crypto.KeyGenerator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.debug.DebugUserRegisterRequest;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.loader.UploadRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataImportService;

/**
 * Provides helper methods for developers.
 */
@RestController
public class DebugController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DebugController.class);

    /**
     * True if user white list is enabled.
     */
    @Value("${security.white-list}")
    private boolean enforceWhiteListCheck;

    /**
     * Folder for storing temporary files.
     */
    @Value("${tmp.folder}")
    private String temporaryPath;

    /**
     * Service for importing data from uploaded files to PostgreSQL and HBase.
     */
    @Autowired
    private IDataImportService fileDataLoaderService;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing group data.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Saves an array of bytes to a file.
     *
     * @param filename the filename.
     * @param bytes the bytes to save.
     * @throws IOException if an I/O exception occurs.
     */
    private void saveFile(String filename, byte[] bytes) throws IOException {
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filename)));
        stream.write(bytes);
        stream.close();
    }

    /**
     * Generates a random MAC address.
     *
     * @return a random MAC address.
     */
    private String generateRandomMacAddress() {
        Random rand = new Random();

        byte[] macAddress = new byte[6];
        rand.nextBytes(macAddress);

        StringBuilder output = new StringBuilder(18);
        for (byte b : macAddress) {

            if (output.length() > 0)
                output.append(":");

            output.append(String.format("%02x", b));
        }

        return output.toString();
    }

    /**
     * Generates an AES key.
     *
     * @return the new AES key.
     * @throws NoSuchAlgorithmException if the algorithm is not supported.
     */
    private String generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return Base64.encodeBase64URLSafeString(keyGen.generateKey().getEncoded());
    }

    /**
     * Registers all users found in the white list that have not yet been assigned to an account.
     *
     * @param request a default password for all new accounts.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/debug/user/create", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse createUsers(@RequestBody DebugUserRegisterRequest request) {
        RestResponse response = new RestResponse();

        try {
            if (ArrayUtils.contains(this.getActiveProfiles(), "development")) {
                String password = request.getPassword();

                if (StringUtils.isBlank(password)) {
                    response.add("DEBUG_USER_REGISTER_EMPTY_PASSWORD", "Failed to register users. A password is required.");
                } else {

                    for (AccountActivity entry : userRepository.getAccountActivity()) {
                        if (entry.getAccountRegisteredOn() == null) {

                            Account account = new Account();
                            account.setUsername(entry.getUsername());
                            account.setPassword(password);

                            userRepository.createUser(account);
                        }
                    }

                }
            } else {
                response.add(SharedErrorCode.UNKNOWN, "Profile [development] is not enabled.");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(SharedErrorCode.UNKNOWN, "Failed to register users.");
        }

        return response;
    }

    /**
     * Registers a new amphiro b1 device to all registered users that have no device registrations
     * in their profile.
     *
     * @param user the currently authenticated user.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/debug/amphiro/create", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse registerAmphiro(@AuthenticationPrincipal AuthenticatedUser user) {
        RestResponse response = new RestResponse();

        try {
            if (ArrayUtils.contains(this.getActiveProfiles(), "development")) {

                DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
                deviceQuery.setType(EnumDeviceType.AMPHIRO);

                ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();
                properties.add(new KeyValuePair("debug.autogenerate", (new DateTime(DateTimeZone.UTC)).toString()));

                for (UUID userKey : groupRepository.getUtilityByIdMemberKeys(user.getId())) {
                    if (deviceRepository.getUserDevices(userKey, deviceQuery).size() == 0) {

                        deviceRepository.createAmphiroDevice(userKey, "Amphiro #1", generateRandomMacAddress(),
                                        generateAesKey(), properties);

                    }
                }
            } else {
                response.add(SharedErrorCode.UNKNOWN, "Profile [development] is not enabled.");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(SharedErrorCode.UNKNOWN, "Failed to register users.");
        }

        return response;
    }

    /**
     * Uploads a data file to the server and perform an action on it e.g. import test data
     * for amphiro b1 devices.
     *
     * @param user the currently authenticated user.
     * @param request the upload file and action.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/debug/amphiro/data/generate", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse upload(@AuthenticationPrincipal AuthenticatedUser user, UploadRequest request) {
        RestResponse response = new RestResponse();

        try {
            if (ArrayUtils.contains(this.getActiveProfiles(), "development")) {
                if (request.getFiles() != null) {
                    FileUtils.forceMkdir(new File(temporaryPath));

                    switch (request.getType()) {
                        case AMPHIRO_DATA:
                            // Check time zone
                            String timezone = request.getTimezone();

                            Set<String> zones = DateTimeZone.getAvailableIDs();

                            if (StringUtils.isBlank(timezone)) {
                                response.add(SharedErrorCode.INVALID_TIME_ZONE,
                                                this.getMessage(SharedErrorCode.INVALID_TIME_ZONE));
                            } else if (!zones.contains(timezone)) {
                                Map<String, Object> properties = ImmutableMap.<String, Object> builder()
                                                .put("timezone", timezone).build();

                                response.add(SharedErrorCode.TIMEZONE_NOT_FOUND,
                                                this.getMessage(SharedErrorCode.TIMEZONE_NOT_FOUND, properties));
                            }

                            if (response.getSuccess()) {
                                if ((request.getFiles() != null) && (request.getFiles().length == 1)) {
                                    MultipartFile file = request.getFiles()[0];
                                    String filename = Paths.get(temporaryPath,
                                                                UUID.randomUUID().toString() + "-" + file.getOriginalFilename()).toString();

                                    this.saveFile(filename, file.getBytes());

                                    this.fileDataLoaderService.importRandomAmphiroSessions(user.getUtilityId(),
                                                                                           filename,
                                                                                           DateTimeZone.forID(request.getTimezone()));
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            } else {
                response.add(SharedErrorCode.UNKNOWN, "Profile [development] is not enabled.");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(SharedErrorCode.UNKNOWN, "Failed to upload file.");
        }

        return response;
    }
}
