package eu.daiad.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.loader.ImportWaterMeterFileConfiguration;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IAmphiroTimeOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;

@Service
public class FileDataLoaderService implements IFileDataLoaderService {

	private static final Log logger = LogFactory.getLog(FileDataLoaderService.class);

	@Autowired
	Environment environment;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IAmphiroTimeOrderedRepository amphiroTimeOrderedRepository;

	@Autowired
	private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

	@Override
	public void importWaterMeter(ImportWaterMeterFileConfiguration configuration) throws ApplicationException {
		File input = new File(configuration.getFilename());

		if (!input.exists()) {
			throw new ApplicationException(SharedErrorCode.FILE_DOES_NOT_EXIST).set("filename",
							configuration.getFilename());
		}

		XSSFWorkbook book = null;
		FileInputStream fis = null;

		Pattern allowedFilenames = Pattern.compile(".*\\.xls$|.*\\.xlsx$");

		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), configuration
						.getSourceReferenceSystem().getSrid());

		if (allowedFilenames.matcher(input.getName()).matches()) {
			try {
				fis = new FileInputStream(input);
				book = new XSSFWorkbook(fis);
				XSSFSheet sheet = book.getSheetAt(0);

				Iterator<Row> itr = sheet.iterator();

				// Iterating over Excel file in Java
				while (itr.hasNext()) {
					Row row = itr.next();

					String username = getStringFromCell(row, configuration.getUsernameCellIndex());
					String serial = getStringFromCell(row, configuration.getMeterIdCellIndex());
					Double longitude = getDoubleFromCell(row, configuration.getLongitudeCellIndex());
					Double latitude = getDoubleFromCell(row, configuration.getLatitudeCellIndex());

					if ((!StringUtils.isBlank(username)) && (!StringUtils.isBlank(serial)) && (longitude != null)
									&& (latitude != null)) {
						Point point = geometryFactory.createPoint(new Coordinate(longitude.doubleValue(), latitude
										.doubleValue()));

						Geometry transformedPoint = point;
						if (configuration.getSourceReferenceSystem().getSrid() != configuration
										.getTargetReferenceSystem().getSrid()) {
							CoordinateReferenceSystem sourceCRS = CRS.decode(configuration.getSourceReferenceSystem()
											.toString());
							CoordinateReferenceSystem targetCRS = CRS.decode(configuration.getTargetReferenceSystem()
											.toString());

							MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
							transformedPoint = JTS.transform(point, transform);
							transformedPoint.setSRID(configuration.getTargetReferenceSystem().getSrid());
						}

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

	private void registerMeter(String filename, String username, String serial, Geometry location) {
		try {
			// Check if meter is already associated with the user
			Device device = this.deviceRepository.getWaterMeterDeviceBySerial(serial);
			if (device == null) {
				ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();
				properties.add(new KeyValuePair("import.file", filename));
				properties.add(new KeyValuePair("import.date", (new DateTime(DateTimeZone.UTC).toString())));

				this.deviceRepository.createMeterDevice(username, serial, properties, location);
			} else {
				// Update device location
				this.deviceRepository.updateMeterLocation(username, serial, location);
			}
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

	@Override
	public void importRandomAmphiroSessions(String filename, String timezone) throws ApplicationException {

		this.importRandomAmphiroSessions(filename, DateTimeZone.forID(timezone));
	}

	@Override
	public void importRandomAmphiroSessions(String filename, DateTimeZone timezone) throws ApplicationException {
		if (!ArrayUtils.contains(environment.getActiveProfiles(), "development")) {
			return;
		}

		File input = new File(filename);
		Scanner scan = null;

		if (!input.exists()) {
			throw new ApplicationException(SharedErrorCode.FILE_DOES_NOT_EXIST).set("filename", filename);
		}

		try {
			// Initialize samples
			ArrayList<AmphiroSampleSession> samples = new ArrayList<AmphiroSampleSession>();

			scan = new Scanner(new File(filename));
			String line = "";

			while (scan.hasNextLine()) {
				line = scan.nextLine();

				String[] split = line.split("\t");

				AmphiroSampleSession sample = new AmphiroSampleSession();

				sample.temperature = Float.parseFloat(split[8]);
				sample.volume = Float.parseFloat(split[7]);
				sample.flow = Float.parseFloat(split[9]);
				sample.duration = Integer.parseInt(split[5]);

				sample.energy = (sample.volume * 4 * ((sample.temperature > 20) ? (sample.temperature - 20) : 0)) / 3412;

				samples.add(sample);
			}
			scan.close();
			scan = null;

			if (samples.size() == 0) {
				return;
			}

			this.generateTimeOrderedSessions(samples, timezone);

			this.generateIndexOrderedSessions(samples, timezone);
		} catch (ApplicationException ex) {
			// Ignore
		} catch (Exception ex) {
			logger.error(String.format("Failed to load random Amphiro data from file [%s].", filename), ex);
		}
	}

	private void generateTimeOrderedSessions(ArrayList<AmphiroSampleSession> samples, DateTimeZone timezone) {
		DateTime now = new DateTime();

		long startDate = (new DateTime(now.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC)).getMillis();
		long endDate = (new DateTime(now.getYear(), 12, now.dayOfMonth().getMaximumValue(), 23, 59, 59,
						DateTimeZone.UTC)).getMillis();

		// Process users
		Random rand = new Random();
		int sampleIndex = 0;

		AmphiroMeasurementTimeIntervalQuery sessionQuery = new AmphiroMeasurementTimeIntervalQuery();
		sessionQuery.setStartDate(startDate);
		sessionQuery.setEndDate(endDate);
		sessionQuery.setGranularity(4);

		DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
		deviceQuery.setType(EnumDeviceType.AMPHIRO);

		for (UUID userKey : userRepository.getUserKeysForUtility()) {
			sessionQuery.setUserKey(userKey);

			for (Device device : deviceRepository.getUserDevices(userKey, deviceQuery)) {
				sessionQuery.setDeviceKey(new UUID[] { device.getKey() });

				AmphiroMeasurementTimeIntervalQueryResult existingSessions = amphiroTimeOrderedRepository
								.searchMeasurements(timezone, sessionQuery);
				if ((existingSessions.getSeries() == null)
								|| (existingSessions.getSeries().get(0).getPoints().size() == 0)) {
					long showerId = 0;

					AmphiroMeasurementCollection data = new AmphiroMeasurementCollection();
					data.setDeviceKey(device.getKey());
					ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();

					for (int i = 0; i < 1500; i++) {
						sampleIndex = (sampleIndex + rand.nextInt(samples.size())) % samples.size();
						showerId++;

						AmphiroSession session = new AmphiroSession();
						session.setDuration(samples.get(sampleIndex).duration);
						session.setEnergy(samples.get(sampleIndex).energy);
						session.setFlow(samples.get(sampleIndex).flow);
						session.setHistory(false);
						session.setId(showerId);
						session.setTemperature(samples.get(sampleIndex).temperature);
						session.setVolume(samples.get(sampleIndex).volume);

						session.setTimestamp(startDate + (long) ((endDate - startDate) * (rand.nextFloat())));

						sessions.add(session);
					}

					data.setSessions(sessions);
					amphiroTimeOrderedRepository.storeData(userKey, data);
				}
			}
		}
	}

	private void generateIndexOrderedSessions(ArrayList<AmphiroSampleSession> samples, DateTimeZone timezone) {
		DateTime now = new DateTime();

		long startDate = (new DateTime(now.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC)).getMillis();
		long endDate = (new DateTime(now.getYear(), 12, now.dayOfMonth().getMaximumValue(), 23, 59, 59,
						DateTimeZone.UTC)).getMillis();

		Random rand = new Random();
		int sampleIndex = 0;

		AmphiroMeasurementIndexIntervalQuery sessionQuery = new AmphiroMeasurementIndexIntervalQuery();
		sessionQuery.setType(EnumIndexIntervalQuery.SLIDING);
		sessionQuery.setLength(10);

		DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
		deviceQuery.setType(EnumDeviceType.AMPHIRO);

		for (UUID userKey : userRepository.getUserKeysForUtility()) {
			sessionQuery.setUserKey(userKey);

			for (Device device : deviceRepository.getUserDevices(userKey, deviceQuery)) {
				sessionQuery.setDeviceKey(new UUID[] { device.getKey() });

				AmphiroMeasurementIndexIntervalQueryResult existingSessions = amphiroIndexOrderedRepository
								.searchMeasurements(timezone, sessionQuery);

				if ((existingSessions.getSeries() == null)
								|| (existingSessions.getSeries().get(0).getPoints().size() == 0)) {
					long showerId = 0;

					AmphiroMeasurementCollection data = new AmphiroMeasurementCollection();
					data.setDeviceKey(device.getKey());
					ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();

					for (int i = 0; i < 1500; i++) {
						sampleIndex = (sampleIndex + rand.nextInt(samples.size())) % samples.size();
						showerId++;

						AmphiroSession session = new AmphiroSession();
						session.setDuration(samples.get(sampleIndex).duration);
						session.setEnergy(samples.get(sampleIndex).energy);
						session.setFlow(samples.get(sampleIndex).flow);
						session.setHistory(false);
						session.setId(showerId);
						session.setTemperature(samples.get(sampleIndex).temperature);
						session.setVolume(samples.get(sampleIndex).volume);

						session.setTimestamp(startDate + (long) ((endDate - startDate) * (rand.nextFloat())));

						sessions.add(session);
					}

					data.setSessions(sessions);
					amphiroIndexOrderedRepository.storeData(userKey, data);
				}
			}
		}
	}

	public static class AmphiroSampleSession {

		public int duration;

		public float temperature;

		public float volume;

		public float energy;

		public float flow;

	}
}
