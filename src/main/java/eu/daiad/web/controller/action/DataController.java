package eu.daiad.web.controller.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ActionErrorCode;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ResourceNotFoundException;
import eu.daiad.web.model.export.DownloadFileResponse;
import eu.daiad.web.model.export.ExportUserDataQuery;
import eu.daiad.web.model.export.ExportUserDataRequest;
import eu.daiad.web.model.query.DataQueryRequest;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.SpatialDataPoint;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IExportService;

@Controller
public class DataController extends BaseController {

	private static final Log logger = LogFactory.getLog(DataController.class);

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private IExportService exportService;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	private void registerMeter(String filename, String username, String serial, Geometry location) {
		try {
			// Check if meter is already associated with the user
			Device device = this.deviceRepository.getWaterMeterDeviceBySerial(serial);
			if (device != null) {
				return;
			}

			ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();
			properties.add(new KeyValuePair("import.file", filename));
			properties.add(new KeyValuePair("import.date", (new DateTime(DateTimeZone.UTC).toString())));

			this.deviceRepository.createMeterDevice(username, serial, properties, location);
		} catch (ApplicationException ex) {
			// Ignore
		} catch (Exception ex) {
			logger.error(String.format("Failed to register device [%s] to user [%s].", username, serial), ex);
		}
	}

	private String getStringFromCell(Row row, int index) {
		Cell cell = row.getCell(index);

		if ((cell != null) && (cell.getCellType() == Cell.CELL_TYPE_STRING)) {
			return cell.getStringCellValue();
		}
		return null;
	}

	private Double getDoubleFromCell(Row row, int index) {
		Cell cell = row.getCell(index);

		if ((cell != null) && (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
			return cell.getNumericCellValue();
		}
		return null;
	}

	private void parse(File input) {
		XSSFWorkbook book = null;
		FileInputStream fis = null;

		Pattern allowedFilenames = Pattern.compile(".*\\.xls$|.*\\.xlsx$");

		// EPSG:25830
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25830);

		if (allowedFilenames.matcher(input.getName()).matches()) {
			try {
				fis = new FileInputStream(input);
				book = new XSSFWorkbook(fis);
				XSSFSheet sheet = book.getSheetAt(0);

				Iterator<Row> itr = sheet.iterator();

				// Iterating over Excel file in Java
				while (itr.hasNext()) {
					Row row = itr.next();

					String username = getStringFromCell(row, 0);
					String serial = getStringFromCell(row, 3);
					Double longitude = getDoubleFromCell(row, 4);
					Double latitude = getDoubleFromCell(row, 5);

					if ((!StringUtils.isBlank(username)) && (!StringUtils.isBlank(serial)) && (longitude != null)
									&& (latitude != null)) {
						Point point = geometryFactory.createPoint(new Coordinate(longitude.doubleValue(), latitude
										.doubleValue()));

						CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:25830");
						CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");

						MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
						Geometry transformedPoint = JTS.transform(point, transform);
						transformedPoint.setSRID(4326);

						this.registerMeter(input.getName(), username, serial, transformedPoint);
					}
				}
			} catch (FactoryException fe) {
				logger.warn(fe);
			} catch (Exception ie) {
				logger.error(ie);
			} finally {
				try {
					if (book != null) {
						book.close();
						book = null;
					}
					if (fis != null) {
						fis.close();
						fis = null;
					}
				} catch (Exception ex) {
					logger.error(String.format("Failed to release resources for file [%s]", input.getName()));
				}
			}
		}
	}

	@RequestMapping(value = "/action/upload", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RestResponse upload(@RequestParam("files") MultipartFile[] files) {
		RestResponse response = new RestResponse();

		try {
			// Get the filename and build the local file path (be sure that the
			// application have write permissions on such directory)
			if (files != null) {
				for (MultipartFile file : files) {
					String filename = file.getOriginalFilename();
					String filepath = Paths.get(temporaryPath, filename).toString();

					// Save the file locally
					BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
					stream.write(file.getBytes());
					stream.close();

					this.parse(new File(filepath));
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add("FILE_UPLOAD_ERROR", "Failed to upload file.");
		}

		return response;
	}

	@RequestMapping(value = "/action/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RestResponse query(@RequestBody DataQueryRequest data) {
		RestResponse response = new RestResponse();

		try {
			DataQueryResponse result = new DataQueryResponse();

			SpatialDataPoint point = new SpatialDataPoint();

			point.setGeometry(data.getQuery().getSpatial().getGeometry());

			point.setLabel("Alicante");
			point.setPopulation(3);
			point.setTimestamp((new DateTime()).getMillis());

			point.getValues().put(EnumMetric.SUM.toString(), 1293.34);
			point.getValues().put(EnumMetric.COUNT.toString(), 20);

			result.getPoints().add(point);
			return result;
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

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
					userQuery.setTimezone(data.getTimezone());

					String token = this.exportService.export(userQuery);

					response = new DownloadFileResponse(token);

					break;
				default:
					throw new ApplicationException(ActionErrorCode.EXPORT_TYPE_NOT_SUPPORTED).set("type",
									data.getType());
			}
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

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
