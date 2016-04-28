package eu.daiad.web.controller.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ActionErrorCode;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ResourceNotFoundException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.export.DownloadFileResponse;
import eu.daiad.web.model.export.ExportUserDataQuery;
import eu.daiad.web.model.export.ExportUserDataRequest;
import eu.daiad.web.model.loader.ImportWaterMeterFileConfiguration;
import eu.daiad.web.model.loader.UploadRequest;
import eu.daiad.web.model.query.DataQueryRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.spatial.ReferenceSystem;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.IExportService;
import eu.daiad.web.service.IFileDataLoaderService;
import eu.daiad.web.service.IWaterMeterDataLoaderService;

@Controller
public class DataController extends BaseController {

	private static final Log logger = LogFactory.getLog(DataController.class);

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private IExportService exportService;

	@Autowired
	private IDataService dataService;

	@Autowired
	private IFileDataLoaderService fileDataLoaderService;

	@Autowired
	private IWaterMeterDataLoaderService waterMeterDataLoaderService;

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

	@RequestMapping(value = "/action/upload", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RestResponse upload(UploadRequest request) {
		RestResponse response = new RestResponse();

		try {
			// Get the filename and build the local file path (be sure that the
			// application have write permissions on such directory)
			if (request.getFiles() != null) {
				FileUtils.forceMkdir(new File(temporaryPath));

				switch (request.getType()) {
					case METER:
						// Check SRID
						Integer srid = request.getSrid();

						if (srid == null) {
							response.add(SharedErrorCode.INVALID_SRID, this.getMessage(SharedErrorCode.INVALID_SRID));
						}

						if (response.getSuccess()) {
							for (MultipartFile file : request.getFiles()) {
								String filename = Paths.get(temporaryPath,
												UUID.randomUUID().toString() + "-" + file.getOriginalFilename())
												.toString();

								this.saveFile(filename, file.getBytes());

								ImportWaterMeterFileConfiguration configuration = new ImportWaterMeterFileConfiguration(
												filename);
								configuration.setSourceReferenceSystem(new ReferenceSystem(request.getSrid()));

								this.fileDataLoaderService.importWaterMeter(configuration);
							}
						}
						break;
					case METER_DATA:
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
							for (MultipartFile file : request.getFiles()) {
								String filename = Paths.get(temporaryPath,
												UUID.randomUUID().toString() + "-" + file.getOriginalFilename())
												.toString();

								this.saveFile(filename, file.getBytes());

								this.waterMeterDataLoaderService.parse(filename, request.getTimezone());
							}
						}
						break;
					default:
						break;
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(SharedErrorCode.UNKNOWN, "Failed to upload file.");
		}

		return response;
	}

	@RequestMapping(value = "/action/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RestResponse query(@RequestBody DataQueryRequest data) {
		RestResponse response = new RestResponse();

		try {
			return dataService.execute(data.getQuery());
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/data/export", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RestResponse export(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody ExportUserDataRequest data) {
		RestResponse response = new RestResponse();

		try {
			switch (data.getType()) {
				case USER_DATA:
					ExportUserDataQuery userQuery = new ExportUserDataQuery();

					// Get user
					AuthenticatedUser owner = userRepository.getUserByUtilityAndKey(user.getUtilityId(),
									data.getUserKey());

					userQuery.setUserKey(data.getUserKey());
					userQuery.setUsername(owner.getUsername());

					// Get devices
					ArrayList<Device> devices = deviceRepository.getUserDevices(owner.getKey(),
									new DeviceRegistrationQuery());

					for (Device d : devices) {
						boolean fetch = false;

						if ((data.getDeviceKeys() == null) || (data.getDeviceKeys().length == 0)) {
							fetch = true;
						} else {
							for (UUID deviceKey : data.getDeviceKeys()) {
								if (d.getKey().equals(deviceKey)) {
									fetch = true;
									break;
								}
							}
						}
						if (fetch) {
							switch (d.getType()) {
								case AMPHIRO:
									userQuery.getAmphiroKeys().add(d.getKey());
									userQuery.getAmphiroNames().add(((AmphiroDevice) d).getName());

									break;
								case METER:
									userQuery.getMeterKeys().add(d.getKey());
									userQuery.getMeterNames().add(((WaterMeterDevice) d).getSerial());

									break;
								default:
									// Ignore device
							}
						}
					}

					// Set time constraints
					userQuery.setStartDateTime(data.getStartDateTime());
					userQuery.setEndDateTime(data.getEndDateTime());

					if (StringUtils.isBlank(data.getTimezone())) {
						userQuery.setTimezone(owner.getTimezone());
					} else {
						userQuery.setTimezone(data.getTimezone());
					}

					String token = this.exportService.export(userQuery);

					response = new DownloadFileResponse(token);

					break;
				default:
					throw new ApplicationException(ActionErrorCode.EXPORT_TYPE_NOT_SUPPORTED).set("type",
									data.getType());
			}
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/data/download/{token}", method = RequestMethod.GET)
	@Secured({ "ROLE_ADMIN" })
	public ResponseEntity<InputStreamResource> download(@PathVariable("token") String token) {
		try {
			File path = new File(temporaryPath);

			File file = new File(path, token + ".zip");

			if (file.exists()) {
				FileSystemResource fileResource = new FileSystemResource(file);

				HttpHeaders headers = new HttpHeaders();
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");

				return ResponseEntity.ok().headers(headers).contentLength(fileResource.contentLength())
								.contentType(MediaType.parseMediaType("application/zip"))
								.body(new InputStreamResource(fileResource.getInputStream()));
			}
		} catch (Exception ex) {
			logger.error(String.format("File [%s] was not found.", token), ex);
		}

		throw new ResourceNotFoundException();
	}
}
