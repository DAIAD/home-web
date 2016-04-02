package eu.daiad.web.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.connector.RemoteFileAttributes;
import eu.daiad.web.connector.SecureFileTransferConnector;
import eu.daiad.web.domain.admin.Upload;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.loader.DataTransferConfiguration;
import eu.daiad.web.model.meter.WaterMeterMeasurement;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

@Service
@Transactional("managementTransactionManager")
public class WaterMeterDataLoader implements IWaterMeterDataLoader {

	private static final Log logger = LogFactory.getLog(WaterMeterDataLoader.class);

	@Autowired
	SecureFileTransferConnector sftConnector;

	@PersistenceContext(unitName = "management")
	EntityManager entityManager;

	@Autowired
	IDeviceRepository deviceRepository;

	@Autowired
	IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@Override
	public void load(DataTransferConfiguration config) {
		try {
			Set<String> zones = DateTimeZone.getAvailableIDs();
			if (config.getTimezone() == null) {
				config.setTimezone("UTC");
			}
			if (!zones.contains(config.getTimezone())) {
				throw new ExportException(String.format("Time zone [%s] is not supported.", config.getTimezone()));
			}

			ArrayList<RemoteFileAttributes> files = this.sftConnector.ls(config.getSftpProperties(),
							config.getRemoteFolder());

			String qlString = "select u from upload u where u.remoteFolder = :remoteFolder and u.remoteFilename = :remoteFilename and u.size = :fileSize";

			for (RemoteFileAttributes f : files) {

				TypedQuery<Upload> uploadQuery = entityManager.createQuery(qlString, Upload.class).setFirstResult(0)
								.setMaxResults(1);

				uploadQuery.setParameter("remoteFolder", f.getRemoteFolder());
				uploadQuery.setParameter("remoteFilename", f.getFilename());
				uploadQuery.setParameter("fileSize", f.getSize());

				List<Upload> uploads = uploadQuery.getResultList();
				if (uploads.size() == 0) {
					Upload upload = new Upload();

					upload.setSource(f.getSource());
					upload.setRemoteFolder(f.getRemoteFolder());
					upload.setRemoteFilename(f.getFilename());

					upload.setSize(f.getSize());
					upload.setModifiedOn(f.getModifiedOn());

					upload.setLocalFolder(config.getLocalFolder());
					upload.setLocalFilename(UUID.randomUUID().toString() + "."
									+ FilenameUtils.getExtension(f.getFilename()));

					String target = FilenameUtils.concat(config.getLocalFolder(), upload.getLocalFilename());

					upload.setUploadStartedOn(new DateTime());
					this.sftConnector
									.get(config.getSftpProperties(), config.getRemoteFolder(), f.getFilename(), target);
					upload.setUploadCompletedOn(new DateTime());

					upload.setProcessingStartedOn(new DateTime());
					this.parse(config, upload);
					upload.setProcessingCompletedOn(new DateTime());

					this.entityManager.persist(upload);
					this.entityManager.flush();
				}
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex);
		}
	}

	private void parse(DataTransferConfiguration config, Upload upload) {
		Scanner scan = null;
		String filename = FilenameUtils.concat(upload.getLocalFolder(), upload.getLocalFilename());

		// ABCD;ABCD;METER;17/02/2014 11:13:45;867;2;
		String line = "";

		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").withZone(
						DateTimeZone.forID(config.getTimezone()));

		int index = 0, processedRows = 0, skippedRows = 0;

		try {
			scan = new Scanner(new File(filename));

			this.waterMeterMeasurementRepository.open();

			while (scan.hasNextLine()) {
				index++;
				line = scan.nextLine();

				float volume, difference;

				String[] tokens = StringUtils.split(line, ";");
				if (tokens.length != 6) {
					skippedRows++;
				} else {
					String serial = tokens[2];

					DateTime timestamp;
					try {
						timestamp = formatter.parseDateTime(tokens[3]);
					} catch (Exception ex) {
						logger.error(String.format("Failed to parse timestamp [%s] in line [%d] from file [%s].",
										tokens[3], index, filename), ex);
						skippedRows++;
						continue;
					}

					try {
						volume = Float.parseFloat(tokens[4]);
					} catch (Exception ex) {
						logger.error(String.format("Failed to parse volume [%s] in line [%d] from file [%s].",
										tokens[4], index, filename), ex);
						skippedRows++;
						continue;
					}
					try {
						difference = Float.parseFloat(tokens[5]);
					} catch (Exception ex) {
						logger.error(String.format("Failed to parse difference [%s] in line [%d] from file [%s].",
										tokens[5], index, filename), ex);
						skippedRows++;
						continue;
					}

					WaterMeterMeasurementCollection data = new WaterMeterMeasurementCollection();
					ArrayList<WaterMeterMeasurement> measurements = new ArrayList<WaterMeterMeasurement>();
					WaterMeterMeasurement measurement = new WaterMeterMeasurement();

					measurement.setTimestamp(timestamp.getMillis());
					measurement.setVolume(volume);
					measurement.setDifference(difference);

					measurements.add(measurement);

					data.setMeasurements(measurements);

					this.waterMeterMeasurementRepository.storeData(serial, data);

					processedRows++;
				}
			}
		} catch (FileNotFoundException fileEx) {
			logger.error(String.format("File [%s] was not found.", filename), fileEx);
		} catch (IOException ioEx) {
			logger.error("Failed to open connection to HBASE.", ioEx);
		} finally {
			this.waterMeterMeasurementRepository.close();

			upload.setProccessedRows(processedRows);
			upload.setSkippedRows(skippedRows);

			if (scan != null) {
				scan.close();
			}
		}
	}
}
