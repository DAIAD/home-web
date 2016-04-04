package eu.daiad.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.server.ExportException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ExportErrorCode;
import eu.daiad.web.model.export.ExportDataRequest;
import eu.daiad.web.model.export.ExtendedSessionData;
import eu.daiad.web.repository.application.IAmphiroMeasurementRepository;

@Service
public class ExportService implements IExportService {

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private IAmphiroMeasurementRepository amphiroMeasurementRepository;

	@Override
	public String export(ExportDataRequest data) throws ApplicationException {
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("settings.device.name", "Device name");
		properties.put("settings.device.calibrate", "Calibrate");
		properties.put("settings.unit", "Unit");
		properties.put("settings.currency", "Currency");
		properties.put("settings.alarm", "Alarm");
		properties.put("settings.water.cost", "Water cost");
		properties.put("settings.water.temperature-cold", "Cold water temperature");
		properties.put("settings.energy.heating", "Heating system");
		properties.put("settings.energy.efficiency", "Efficiency");
		properties.put("settings.energy.cost", "Energy cost");
		properties.put("settings.energy.solar", "Share of solar");
		properties.put("settings.shower.estimate-per-week", "Estimates showers per week");
		properties.put("settings.shower.time-between-shower", "Time between showers");

		try {
			File path = new File(temporaryPath);

			path.mkdirs();

			if (!path.exists()) {
				throw new ApplicationException("Unable to create temporary path.", ExportErrorCode.PATH_CREATION_FAILED);
			}
			;

			List<ExtendedSessionData> sessions = this.amphiroMeasurementRepository.exportSessions(data);

			if (sessions.size() == 0) {
				throw new ExportException("No data found for the selected criteria.");
			}

			String token = UUID.randomUUID().toString();

			File txtFile = new File(path, token + ".txt");
			File csvFile = new File(path, token + ".csv");
			File zipFile = new File(path, token + ".zip");

			Set<String> zones = DateTimeZone.getAvailableIDs();
			if (data.getTimezone() == null) {
				data.setTimezone("Europe/Athens");
			}
			if (!zones.contains(data.getTimezone())) {
				throw new ExportException(String.format("Time zone [%s] is not supported.", data.getTimezone()));
			}

			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(
							DateTimeZone.forID(data.getTimezone()));

			PrintWriter writer = new PrintWriter(csvFile, StandardCharsets.UTF_8.toString());
			for (int s = 0; s < sessions.size(); s++) {
				ExtendedSessionData session = sessions.get(s);

				writer.format("%s\t%s\t%s\t%s\t%s\t%s\t", session.getUser().getKey(), session.getUser().getUsername(),
								session.getUser().getPostalCode(), session.getDevice().getId(), session.getDevice()
												.getKey(), session.getDevice().getName());

				writer.format("%s\t%d\t%d\t%.4f\t%.4f\t%.4f\t%.4f", session.getDate().toString(formatter),
								session.getId(), session.getDuration(), session.getTemperature(), session.getVolume(),
								session.getFlow(), session.getEnergy());

				if (data.getProperties().size() != 0) {
					Iterator<String> iter = data.getProperties().iterator();
					while (iter.hasNext()) {
						String key = (String) iter.next();

						String value = session.getPropertyByKey(key);
						if (value == null) {
							writer.print("\t");
						} else {
							writer.format("\t%s", value);
						}
					}
				}
				writer.format("\t%s", (session.isHistory() ? "true" : "false"));
				writer.print("\n");
			}
			writer.flush();
			writer.close();

			writer = new PrintWriter(txtFile, StandardCharsets.UTF_8.toString());
			writer.format("%s\n%s\n%s\n%s\n%s\n%s\n", "User unique Id (Database)", "User name (email)",
							"User postal code", "Device unique Id (Mobile Application)", "Device unique Id (Database)",
							"Device name (optional)");

			writer.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n", "Date & Time data received", "Shower Id", "Duration",
							"Temperature", "Volume", "Flow", "Energy");

			if (data.getProperties().size() != 0) {
				Iterator<String> iter = data.getProperties().iterator();
				while (iter.hasNext()) {
					writer.format("%s\n", properties.get(iter.next()));
				}
			}
			writer.format("%s\n", "Real time / historical (true / false)");
			writer.flush();
			writer.close();

			// Compress file
			byte[] buffer = new byte[4096];

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			int len;

			ZipEntry ze = new ZipEntry("export.csv");
			zos.putNextEntry(ze);
			FileInputStream in = new FileInputStream(csvFile);
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			in.close();
			zos.flush();
			zos.closeEntry();

			ze = new ZipEntry("readme.txt");
			zos.putNextEntry(ze);
			in = new FileInputStream(txtFile);
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			in.close();
			zos.flush();
			zos.closeEntry();

			zos.close();

			csvFile.delete();
			txtFile.delete();
			return token;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex);
		}
	}
}
