package eu.daiad.web.controller.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.debug.DebugUserRegisterRequest;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.loader.UploadRequest;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IFileDataLoaderService;

@Controller
public class DebugController extends BaseController {

	private static final Log logger = LogFactory.getLog(DebugController.class);

	@Value("${security.white-list}")
	private boolean enforceWhiteListCheck;

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private IFileDataLoaderService fileDataLoaderService;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	Environment environment;

	private void saveFile(String filename, byte[] bytes) throws IOException {
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filename)));
		stream.write(bytes);
		stream.close();
	}

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

	private String generateAesKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		return Base64.encodeBase64URLSafeString(keyGen.generateKey().getEncoded());
	}

	@RequestMapping(value = "/action/debug/user/create", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RestResponse createUsers(@RequestBody DebugUserRegisterRequest request) {
		RestResponse response = new RestResponse();

		try {
			if (ArrayUtils.contains(environment.getActiveProfiles(), "development")) {
				String password = request.getPassword();

				if (StringUtils.isBlank(password)) {
					response.add("DEBUG_USER_REGISTER_EMPTY_PASSWORD",
									"Failed to register users. A password is required.");
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
				response.add("DEBUG_USER_INVALID_PROFILE", "Profile [development] is not enabled.");
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add("DEBUG_USER_REGISTER_UNKNOWN_ERROR", "Failed to register users.");
		}

		return response;
	}

	@RequestMapping(value = "/action/debug/amphiro/create", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RestResponse registerAmphiro() {
		RestResponse response = new RestResponse();

		try {
			if (ArrayUtils.contains(environment.getActiveProfiles(), "development")) {

				DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
				deviceQuery.setType(EnumDeviceType.AMPHIRO);

				ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();
				properties.add(new KeyValuePair("debug.autogenerate", (new DateTime(DateTimeZone.UTC)).toString()));

				for (UUID userKey : userRepository.getUserKeysForUtility()) {
					if (deviceRepository.getUserDevices(userKey, deviceQuery).size() == 0) {

						deviceRepository.createAmphiroDevice(userKey, "Amphiro #1", generateRandomMacAddress(),
										generateAesKey(), properties);

					}
				}
			} else {
				response.add("DEBUG_USER_INVALID_PROFILE", "Profile [development] is not enabled.");
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add("DEBUG_USER_REGISTER_UNKNOWN_ERROR", "Failed to register users.");
		}

		return response;
	}

	@RequestMapping(value = "/action/debug/amphiro/data/generate", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RestResponse upload(UploadRequest request) {
		RestResponse response = new RestResponse();

		if (ArrayUtils.contains(environment.getActiveProfiles(), "development")) {
			try {
				if (request.getFiles() != null) {
					FileUtils.forceMkdir(new File(temporaryPath));

					// Set time zone
					Set<String> zones = DateTimeZone.getAvailableIDs();
					if (request.getTimezone() == null) {
						request.setTimezone("Europe/Athens");
					}
					if (!zones.contains(request.getTimezone())) {
						throw new ApplicationException(SharedErrorCode.TIMEZONE_NOT_FOUND).set("timezone",
										request.getTimezone());
					}

					switch (request.getType()) {
						case AMPHIRO_DATA:
							if ((request.getFiles() != null) && (request.getFiles().length == 1)) {
								MultipartFile file = request.getFiles()[0];
								String filename = Paths.get(temporaryPath,
												UUID.randomUUID().toString() + "-" + file.getOriginalFilename())
												.toString();

								this.saveFile(filename, file.getBytes());

								this.fileDataLoaderService.importRandomAmphiroSessions(filename,
												DateTimeZone.forID(request.getTimezone()));
							}
							break;
						default:
							break;
					}
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);

				response.add("FILE_UPLOAD_ERROR", "Failed to upload file.");
			}
		} else {
			response.add("DEBUG_USER_INVALID_PROFILE", "Profile [development] is not enabled.");
		}

		return response;
	}

}
