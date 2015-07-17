package eu.daiad.web.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.AmphiroDevice;
import eu.daiad.web.model.DataPoint;
import eu.daiad.web.model.DataSeries;
import eu.daiad.web.model.DayIntervalDataPointCollection;
import eu.daiad.web.model.ExportData;
import eu.daiad.web.model.ExtendedSessionData;
import eu.daiad.web.model.HourlyDataPoints;
import eu.daiad.web.model.Measurement;
import eu.daiad.web.model.DeviceMeasurementCollection;
import eu.daiad.web.model.MeasurementQuery;
import eu.daiad.web.model.MeasurementResult;
import eu.daiad.web.model.SessionData;
import eu.daiad.web.model.Shower;
import eu.daiad.web.model.ShowerCollectionQuery;
import eu.daiad.web.model.ShowerCollectionResult;
import eu.daiad.web.model.ShowerDetails;
import eu.daiad.web.model.ShowerQuery;
import eu.daiad.web.model.ShowerResult;
import eu.daiad.web.model.SmartMeterCollectionResult;
import eu.daiad.web.model.SmartMeterDataPoint;
import eu.daiad.web.model.SmartMeterIntervalQuery;
import eu.daiad.web.model.SmartMeterMeasurement;
import eu.daiad.web.model.MeterMeasurementCollection;
import eu.daiad.web.model.SmartMeterQuery;
import eu.daiad.web.model.SmartMeterResult;
import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.security.model.DaiadUser;

@Repository()
@Scope("prototype")
@PropertySource("${hbase.properties}")
public class MeasurementRepository {

	private String quorum;

	@Value("${hbase.data.time.partitions}")
	private short timePartitions;

	private String amphiroTableName;

	private String smartMeterTableName;

	private String columnFamilyName;

	private static final Log logger = LogFactory
			.getLog(MeasurementRepository.class);

	@Autowired
	public MeasurementRepository(
			@Value("${hbase.zookeeper.quorum}") String quorum,
			@Value("${hbase.data.amphiro.table}") String amphiroTableName,
			@Value("${hbase.data.swm.table}") String smartMeterTableName,
			@Value("${hbase.data.amphiro.column-family}") String columnFamilyName) {
		this.quorum = quorum;
		this.amphiroTableName = amphiroTableName;
		this.smartMeterTableName = smartMeterTableName;
		this.columnFamilyName = columnFamilyName;
	}

	public List<ExtendedSessionData> exportDataAmhiroSession(ExportData data)
			throws Exception {
		ArrayList<ExtendedSessionData> sessions = new ArrayList<ExtendedSessionData>();

		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		try {
			if (data != null) {
				Configuration config = HBaseConfiguration.create();
				config.set("hbase.zookeeper.quorum", this.quorum);

				connection = ConnectionFactory.createConnection(config);

				table = connection.getTable(TableName
						.valueOf("daiad:device-sessions-by-time"));
				byte[] columnFamily = Bytes.toBytes("cf");

				DateTime fromDate = data.getFrom();
				DateTime toDate = data.getTo().plusDays(1);

				for (short p = 0; p < timePartitions; p++) {
					Scan scan = new Scan();
					scan.addFamily(columnFamily);

					byte[] partitionBytes = Bytes.toBytes(p);

					long from = fromDate.getMillis() / 1000;
					from = from - (from % 86400);
					byte[] fromBytes = Bytes.toBytes(from);

					long to = toDate.getMillis() / 1000;
					to = to - (to % 86400);
					byte[] toBytes = Bytes.toBytes(to);

					// Scanner row key prefix start
					byte[] rowKey = new byte[partitionBytes.length
							+ fromBytes.length];

					System.arraycopy(partitionBytes, 0, rowKey, 0,
							partitionBytes.length);
					System.arraycopy(fromBytes, 0, rowKey,
							partitionBytes.length, fromBytes.length);

					scan.setStartRow(rowKey);

					// Scanner row key prefix end
					rowKey = new byte[partitionBytes.length + toBytes.length];

					System.arraycopy(partitionBytes, 0, rowKey, 0,
							partitionBytes.length);
					System.arraycopy(toBytes, 0, rowKey, partitionBytes.length,
							toBytes.length);

					scan.setStopRow(rowKey);

					scanner = table.getScanner(scan);

					for (Result r = scanner.next(); r != null; r = scanner
							.next()) {
						NavigableMap<byte[], byte[]> map = r
								.getFamilyMap(columnFamily);

						if (map != null) {
							ExtendedSessionData session = new ExtendedSessionData();

							rowKey = r.getRow();

							long timeBucket = Bytes.toLong(Arrays.copyOfRange(
									r.getRow(), 2, 10));
							long showerId = Bytes.toLong(Arrays.copyOfRange(
									r.getRow(), 42, 50));

							session.setShowerId(showerId);

							for (Entry<byte[], byte[]> entry : map.entrySet()) {
								String qualifier = Bytes.toString(entry
										.getKey());

								switch (qualifier) {
								// User data
								case "u:key":
									session.getUser().setKey(
											new String(entry.getValue(),
													StandardCharsets.UTF_8));
									break;
								case "u:username":
									session.getUser().setUsername(
											new String(entry.getValue(),
													StandardCharsets.UTF_8));
									break;
								case "u:postal":
									session.getUser().setPostalCode(
											new String(entry.getValue(),
													StandardCharsets.UTF_8));
									break;
								// Device data
								case "d:id":
									session.getDevice().setId(
											new String(entry.getValue(),
													StandardCharsets.UTF_8));
									break;
								case "d:key":
									session.getDevice().setKey(
											new String(entry.getValue(),
													StandardCharsets.UTF_8));
									break;
								case "d:name":
									session.getDevice().setName(
											new String(entry.getValue(),
													StandardCharsets.UTF_8));
									break;
								// Measurement data
								case "m:offset":
									int offset = Bytes.toInt(entry.getValue());
									DateTime timestamp = new DateTime(
											(timeBucket + (long) offset) * 1000L);
									session.setTimestamp(timestamp);
									break;
								case "m:t":
									session.setTemperature(Bytes.toFloat(entry
											.getValue()));
									break;
								case "m:v":
									session.setVolume(Bytes.toFloat(entry
											.getValue()));
									break;
								case "m:f":
									session.setFlow(Bytes.toFloat(entry
											.getValue()));
									break;
								case "m:e":
									session.setEnergy(Bytes.toFloat(entry
											.getValue()));
									break;
								case "m:d":
									session.setDuration(Bytes.toInt(entry
											.getValue()));
									break;
								// General data
								case "r:h":
									session.setHistory(Bytes.toBoolean(entry
											.getValue()));
									break;
								default:
									session.addProperty(qualifier, new String(
											entry.getValue(),
											StandardCharsets.UTF_8));
									break;
								}
							}

							sessions.add(session);
						}
					}

					scanner.close();
				}
			}
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}

		return sessions;
	}

	private void storeDataAmhiroSessionByUser(Connection connection,
			DaiadUser user, AmphiroDevice device,
			DeviceMeasurementCollection data) throws Exception {
		Table table = null;
		try {
			if ((data == null) || (data.getSessions() == null)) {
				return;
			}
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName
					.valueOf("daiad:device-sessions-by-user"));
			byte[] columnFamily = Bytes.toBytes("cf");

			for (int i = 0; i < data.getSessions().size(); i++) {
				SessionData s = data.getSessions().get(i);

				byte[] userKey = data.getUserKey().toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKey);

				byte[] deviceKey = data.getDeviceKey().toString()
						.getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				long timestamp = s.getTimestamp().getMillis() / 1000;
				long offset = timestamp % 86400;
				long timeBucket = timestamp - offset;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] showerIdBytes = Bytes.toBytes(s.getShowerId());

				byte[] rowKey = new byte[userKeyHash.length
						+ deviceKeyHash.length + timeBucketBytes.length
						+ showerIdBytes.length];

				System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length,
						deviceKeyHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey,
						(userKeyHash.length + deviceKeyHash.length),
						timeBucketBytes.length);
				System.arraycopy(showerIdBytes, 0, rowKey, (userKeyHash.length
						+ deviceKeyHash.length + timeBucketBytes.length),
						showerIdBytes.length);

				Put put = new Put(rowKey);
				byte[] column;

				// Add user data
				column = Bytes.toBytes("u:key");
				put.addColumn(columnFamily, column, user.getKey().toString()
						.getBytes(StandardCharsets.UTF_8));

				column = Bytes.toBytes("u:username");
				put.addColumn(columnFamily, column, user.getUsername()
						.getBytes(StandardCharsets.UTF_8));

				column = Bytes.toBytes("u:postal");
				put.addColumn(columnFamily, column, user.getPostalCode()
						.getBytes(StandardCharsets.UTF_8));

				// Add device data
				column = Bytes.toBytes("d:id");
				put.addColumn(columnFamily, column, device.getDeviceId()
						.getBytes(StandardCharsets.UTF_8));

				column = Bytes.toBytes("d:key");
				put.addColumn(columnFamily, column, device.getKey().toString()
						.getBytes(StandardCharsets.UTF_8));

				column = Bytes.toBytes("d:name");
				put.addColumn(columnFamily, column,
						device.getName().getBytes(StandardCharsets.UTF_8));

				// Add measurement data
				column = Bytes.toBytes("m:offset");
				put.addColumn(columnFamily, column, Bytes.toBytes((int) offset));

				column = Bytes.toBytes("m:t");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.getTemperature()));

				column = Bytes.toBytes("m:v");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.getVolume()));

				column = Bytes.toBytes("m:f");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

				column = Bytes.toBytes("m:e");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.getEnergy()));

				column = Bytes.toBytes("m:d");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.getDuration()));
				
				column = Bytes.toBytes("r:h");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.isHistory()));

				for (int p = 0, count = s.getProperties().size(); p < count; p++) {
					column = Bytes.toBytes(s.getProperties().get(p).getKey());
					put.addColumn(columnFamily, column, s.getProperties()
							.get(p).getValue().getBytes(StandardCharsets.UTF_8));
				}

				table.put(put);
			}
		} finally {
			if (table != null) {
				table.close();
			}
		}
	}

	private void storeDataAmhiroSessionByTime(Connection connection,
			DaiadUser user, AmphiroDevice device,
			DeviceMeasurementCollection data) throws Exception {
		Table table = null;
		try {
			if ((data == null) || (data.getSessions() == null)) {
				return;
			}
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName
					.valueOf("daiad:device-sessions-by-time"));
			byte[] columnFamily = Bytes.toBytes("cf");

			for (int i = 0; i < data.getSessions().size(); i++) {
				SessionData s = data.getSessions().get(i);

				short partition = (short) (s.getTimestamp().getMillis() % this.timePartitions);
				byte[] partitionBytes = Bytes.toBytes(partition);

				byte[] userKey = data.getUserKey().toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKey);

				byte[] deviceKey = data.getDeviceKey().toString()
						.getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				long timestamp = s.getTimestamp().getMillis() / 1000;
				long offset = timestamp % 86400;
				long timeBucket = timestamp - offset;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] showerIdBytes = Bytes.toBytes(s.getShowerId());

				byte[] rowKey = new byte[partitionBytes.length
						+ timeBucketBytes.length + userKeyHash.length
						+ deviceKeyHash.length + showerIdBytes.length];

				System.arraycopy(partitionBytes, 0, rowKey, 0,
						partitionBytes.length);
				System.arraycopy(timeBucketBytes, 0, rowKey,
						partitionBytes.length, timeBucketBytes.length);
				System.arraycopy(userKeyHash, 0, rowKey,
						(partitionBytes.length + timeBucketBytes.length),
						userKeyHash.length);
				System.arraycopy(
						deviceKeyHash,
						0,
						rowKey,
						(partitionBytes.length + timeBucketBytes.length + userKeyHash.length),
						deviceKeyHash.length);
				System.arraycopy(showerIdBytes, 0, rowKey,
						(partitionBytes.length + timeBucketBytes.length
								+ userKeyHash.length + deviceKeyHash.length),
						showerIdBytes.length);

				Put put = new Put(rowKey);

				byte[] column;

				// Add user data
				column = Bytes.toBytes("u:key");
				put.addColumn(columnFamily, column, user.getKey().toString()
						.getBytes(StandardCharsets.UTF_8));

				column = Bytes.toBytes("u:username");
				put.addColumn(columnFamily, column, user.getUsername()
						.getBytes(StandardCharsets.UTF_8));

				column = Bytes.toBytes("u:postal");
				put.addColumn(columnFamily, column, user.getPostalCode()
						.getBytes(StandardCharsets.UTF_8));

				// Add device data
				column = Bytes.toBytes("d:id");
				put.addColumn(columnFamily, column, device.getDeviceId()
						.getBytes(StandardCharsets.UTF_8));

				column = Bytes.toBytes("d:key");
				put.addColumn(columnFamily, column, device.getKey().toString()
						.getBytes(StandardCharsets.UTF_8));

				column = Bytes.toBytes("d:name");
				put.addColumn(columnFamily, column,
						device.getName().getBytes(StandardCharsets.UTF_8));

				// Add measurement data

				column = Bytes.toBytes("m:offset");
				put.addColumn(columnFamily, column, Bytes.toBytes((int) offset));

				column = Bytes.toBytes("m:t");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.getTemperature()));

				column = Bytes.toBytes("m:v");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.getVolume()));

				column = Bytes.toBytes("m:f");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

				column = Bytes.toBytes("m:e");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.getEnergy()));

				column = Bytes.toBytes("m:d");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.getDuration()));

				column = Bytes.toBytes("r:h");
				put.addColumn(columnFamily, column,
						Bytes.toBytes(s.isHistory()));

				for (int p = 0, count = s.getProperties().size(); p < count; p++) {
					column = Bytes.toBytes(s.getProperties().get(p).getKey());
					put.addColumn(columnFamily, column, s.getProperties()
							.get(p).getValue().getBytes(StandardCharsets.UTF_8));
				}

				table.put(put);
			}
		} finally {
			if (table != null) {
				table.close();
			}
		}
	}

	private void storeDataAmhiroMeasurements(Connection connection,
			DaiadUser user, AmphiroDevice device,
			DeviceMeasurementCollection data) throws Exception {
		Table table = null;
		try {
			if ((data == null) || (data.getMeasurements() == null)) {
				return;
			}
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName
					.valueOf(this.amphiroTableName));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				Measurement m = data.getMeasurements().get(i);

				if (m.volume <= 0) {
					continue;
				}

				byte[] applicationKey = data.getUserKey().toString()
						.getBytes("UTF-8");
				byte[] applicationKeyHash = md.digest(applicationKey);

				byte[] deviceId = data.getDeviceKey().toString()
						.getBytes("UTF-8");
				byte[] deviceIdHash = md.digest(deviceId);

				m.timestamp = m.timestamp / 1000;

				long timeSlice = m.timestamp % 3600;
				byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);
				if (timeSliceBytes.length != 2) {
					throw new RuntimeException("Invalid byte array length!");
				}

				long timeBucket = m.timestamp - timeSlice;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] rowKey = new byte[applicationKeyHash.length
						+ deviceIdHash.length + timeBucketBytes.length];
				System.arraycopy(applicationKeyHash, 0, rowKey, 0,
						applicationKeyHash.length);
				System.arraycopy(deviceIdHash, 0, rowKey,
						applicationKeyHash.length, deviceIdHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey,
						(applicationKeyHash.length + deviceIdHash.length),
						timeBucketBytes.length);

				Put p = new Put(rowKey);

				byte[] column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("si")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.showerId));

				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("st")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.showerTime));
				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.volume));
				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("t")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.temperature));
				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("e")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.energy));

				table.put(p);
			}
		} finally {
			if (table != null) {
				table.close();
			}
		}
	}

	public void storeDataAmphiro(DaiadUser user, AmphiroDevice device,
			DeviceMeasurementCollection data) {
		Connection connection = null;
		try {
			if (data == null) {
				return;
			}
			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			this.storeDataAmhiroSessionByUser(connection, user, device, data);
			this.storeDataAmhiroSessionByTime(connection, user, device, data);

			this.storeDataAmhiroMeasurements(connection, user, device, data);

			connection.close();
		} catch (RuntimeException ex) {
			logger.error("Malformed data found.", ex);
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		} finally {
			try {
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error("Unhandled exception has occurred.", ex);
			}
		}
	}

	public void storeDataSmartMeter(MeterMeasurementCollection data) {
		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.smartMeterTableName));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] applicationKey = data.getUserKey().toString()
					.getBytes("UTF-8");
			byte[] applicationKeyHash = md.digest(applicationKey);

			byte[] deviceId = data.getDeviceKey().toString().getBytes("UTF-8");
			byte[] deviceIdHash = md.digest(deviceId);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				SmartMeterMeasurement m = data.getMeasurements().get(i);

				if (m.getVolume() <= 0) {
					continue;
				}

				long timestamp = (Long.MAX_VALUE - m.getTimestamp()) / 1000;

				long timeSlice = timestamp % 3600;
				byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);
				if (timeSliceBytes.length != 2) {
					throw new RuntimeException("Invalid byte array length!");
				}

				long timeBucket = timestamp - timeSlice;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] rowKey = new byte[applicationKeyHash.length
						+ deviceIdHash.length + timeBucketBytes.length];
				System.arraycopy(applicationKeyHash, 0, rowKey, 0,
						applicationKeyHash.length);
				System.arraycopy(deviceIdHash, 0, rowKey,
						applicationKeyHash.length, deviceIdHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey,
						(applicationKeyHash.length + deviceIdHash.length),
						timeBucketBytes.length);

				Put p = new Put(rowKey);

				byte[] column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

				table.put(p);
			}
			table.close();
			connection.close();
		} catch (RuntimeException ex) {
			logger.error("Malformed data found.", ex);
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}
	}

	private byte[] getRowKey(byte[] application, byte[] device, DateTime date)
			throws Exception {
		return this.getRowKey(application, device, date.getMillis());
	}

	private byte[] getRowKey(byte[] application, byte[] device, long date)
			throws Exception {
		long timestamp = date / 1000;
		long timeSlice = timestamp % 3600;
		long timeBucket = timestamp - timeSlice;
		byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

		byte[] rowKey = new byte[application.length + device.length + 8];
		System.arraycopy(application, 0, rowKey, 0, application.length);
		System.arraycopy(device, 0, rowKey, application.length, device.length);
		System.arraycopy(timeBucketBytes, 0, rowKey,
				(device.length + device.length), timeBucketBytes.length);

		return rowKey;
	}

	private byte[] appendLength(byte[] array) throws Exception {
		byte[] length = { (byte) array.length };

		return concatenate(length, array);
	}

	private byte[] concatenate(byte[] a, byte[] b) {
		int lengthA = a.length;
		int lengthB = b.length;
		byte[] concat = new byte[lengthA + lengthB];
		System.arraycopy(a, 0, concat, 0, lengthA);
		System.arraycopy(b, 0, concat, lengthA, lengthB);
		return concat;
	}

	public MeasurementResult query(MeasurementQuery query) {
		MeasurementResult data = new MeasurementResult();

		DateTime startDate = new DateTime(query.getStartDate());
		DateTime endDate = new DateTime(query.getEndDate());

		switch (query.getGranularity()) {
		case TemporalConstants.DAILY:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
					0, 0);
			endDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(), 23,
					59, 59);
			break;
		case TemporalConstants.WEEKLY:
			DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
			DateTime sunday = startDate.withDayOfWeek(DateTimeConstants.SUNDAY);
			startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(),
					monday.getDayOfMonth(), 0, 0, 0);
			endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(),
					sunday.getDayOfMonth(), 23, 59, 59);
			break;
		case TemporalConstants.MONTHLY:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), 1, 0, 0, 0);
			endDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.dayOfMonth()
							.getMaximumValue(), 23, 59, 59);
			break;
		}
		data.setSeries(new ArrayList<DataSeries>());

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.amphiroTableName));
			byte[] columnFamily = Bytes.toBytes("m");

			byte[] applicationKey = query.getApplicationKey().toString()
					.getBytes("UTF-8");
			byte[] applicationKeyHash = md.digest(applicationKey);

			byte[] deviceId = query.getDeviceId().toString().getBytes("UTF-8");
			byte[] deviceIdHash = md.digest(deviceId);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getRowKey(applicationKeyHash, deviceIdHash,
					startDate));
			scan.setStopRow(this.getRowKey(applicationKeyHash, deviceIdHash,
					endDate));

			ResultScanner scanner = table.getScanner(scan);

			DataSeries series = new DataSeries();
			series.setDeviceId(query.getDeviceId());
			series.setPoints(new ArrayList<DataPoint>());

			data.getSeries().add(series);

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(),
						32, 40));

				HourlyDataPoints hourlyPoints = new HourlyDataPoints();

				short offset = -1;
				DataPoint point = null;

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					short entryOffset = Bytes.toShort(Arrays.copyOfRange(
							entry.getKey(), 0, 2));

					if (offset != entryOffset) {
						if (point != null) {
							hourlyPoints.add(point);
						}
						offset = entryOffset;
						point = new DataPoint();
						point.timestamp = (timeBucket + (long) offset) * 1000L;
					}

					int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
					byte[] slice = Arrays.copyOfRange(entry.getKey(), 3,
							3 + length);
					String columnQualifier = Bytes.toString(slice);
					if (columnQualifier.equals("v")) {
						point.volume = Bytes.toFloat(entry.getValue());
					} else if (columnQualifier.equals("e")) {
						point.energy = Bytes.toFloat(entry.getValue());
					} else if (columnQualifier.equals("t")) {
						point.temperature = Bytes.toFloat(entry.getValue());
					}
				}
				if (point != null) {
					hourlyPoints.add(point);
				}
				series.getPoints().add(hourlyPoints.average());
			}
			scanner.close();
			table.close();
			connection.close();

			// Second level of aggregation
			switch (query.getGranularity()) {
			case TemporalConstants.MONTHLY:
			case TemporalConstants.WEEKLY:
			case TemporalConstants.NONE:
				int numberOfDays = Days.daysBetween(startDate.toLocalDate(),
						endDate.toLocalDate()).getDays();

				if (numberOfDays > 0) {
					DayIntervalDataPointCollection interval = new DayIntervalDataPointCollection();

					interval.addAll(series.getPoints());

					series.setPoints(interval.getPoints());
				}
				break;
			}
			Collections.sort(series.getPoints(), new Comparator<DataPoint>() {

				public int compare(DataPoint o1, DataPoint o2) {
					if (o1.timestamp <= o2.timestamp) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			return data;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}

	public SmartMeterResult query(SmartMeterQuery query) {
		SmartMeterResult data = new SmartMeterResult();
		data.setDeviceId(query.getDeviceId());

		SmartMeterDataPoint value1 = new SmartMeterDataPoint();
		SmartMeterDataPoint value2 = new SmartMeterDataPoint();

		long lastTimeBucket = 0;
		int bucketCount = 0;
		int valueCount = 0;

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.smartMeterTableName));
			byte[] columnFamily = Bytes.toBytes("m");

			byte[] applicationKey = query.getApplicationKey().toString()
					.getBytes("UTF-8");
			byte[] applicationKeyHash = md.digest(applicationKey);

			byte[] deviceId = query.getDeviceId().getBytes("UTF-8");
			byte[] deviceIdHash = md.digest(deviceId);

			DateTime maxDate = new DateTime();

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getRowKey(applicationKeyHash, deviceIdHash,
					Long.MAX_VALUE - maxDate.getMillis()));
			scan.setCaching(1);

			ResultScanner scanner = table.getScanner(scan);

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				if (bucketCount > 2) {
					break;
				}

				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(),
						32, 40));
				if (lastTimeBucket != timeBucket) {
					bucketCount++;
				}
				lastTimeBucket = timeBucket;

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					short offset = Bytes.toShort(Arrays.copyOfRange(
							entry.getKey(), 0, 2));

					long timestamp = Long.MAX_VALUE
							- ((timeBucket + (long) offset) * 1000L);

					int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
					byte[] slice = Arrays.copyOfRange(entry.getKey(), 3,
							3 + length);
					String columnQualifier = Bytes.toString(slice);
					if (columnQualifier.equals("v")) {
						valueCount++;
						if (value1.timestamp < timestamp) {
							value2.timestamp = value1.timestamp;
							value2.volume = value1.volume;

							value1.timestamp = timestamp;
							value1.volume = Bytes.toFloat(entry.getValue());
						} else if (value2.timestamp < timestamp) {
							value2.timestamp = timestamp;
							value2.volume = Bytes.toFloat(entry.getValue());
						}
					}
				}
			}

			scanner.close();
			table.close();
			connection.close();

			switch (valueCount) {
			case 0:
				// No value found
				break;
			case 1:
				data.setValue1(value1);
				data.setValue2(value1);
			default:
				data.setValue1(value1);
				data.setValue2(value2);
			}

			return data;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}

	public SmartMeterCollectionResult query(SmartMeterIntervalQuery query) {
		DateTime startDate = new DateTime(query.getStartDate());
		DateTime endDate = new DateTime(query.getEndDate());

		switch (query.getGranularity()) {
		case TemporalConstants.DAILY:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
					0, 0);
			endDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(), 23,
					59, 59);
			break;
		case TemporalConstants.WEEKLY:
			DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
			DateTime sunday = startDate.withDayOfWeek(DateTimeConstants.SUNDAY);
			startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(),
					monday.getDayOfMonth(), 0, 0, 0);
			endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(),
					sunday.getDayOfMonth(), 23, 59, 59);
			break;
		case TemporalConstants.MONTHLY:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), 1, 0, 0, 0);
			endDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.dayOfMonth()
							.getMaximumValue(), 23, 59, 59);
			break;
		default:
			return new SmartMeterCollectionResult(-1,
					"Granularity level not supported.");
		}

		DateTime now = new DateTime();
		if (now.getMillis() < endDate.getMillis()) {
			endDate = now.plusHours(1);
		}

		SmartMeterCollectionResult data = new SmartMeterCollectionResult(
				startDate.getMillis(), endDate.getMillis());

		data.setDeviceId(query.getDeviceId());
		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.smartMeterTableName));
			byte[] columnFamily = Bytes.toBytes("m");

			byte[] applicationKey = query.getApplicationKey().toString()
					.getBytes("UTF-8");
			byte[] applicationKeyHash = md.digest(applicationKey);

			byte[] deviceId = query.getDeviceId().getBytes("UTF-8");
			byte[] deviceIdHash = md.digest(deviceId);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getRowKey(applicationKeyHash, deviceIdHash,
					Long.MAX_VALUE - endDate.getMillis()));

			ResultScanner scanner = table.getScanner(scan);

			boolean isScanCompleted = false;

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(),
						32, 40));

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					short offset = Bytes.toShort(Arrays.copyOfRange(
							entry.getKey(), 0, 2));

					long timestamp = Long.MAX_VALUE
							- ((timeBucket + (long) offset) * 1000L);

					int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
					byte[] slice = Arrays.copyOfRange(entry.getKey(), 3,
							3 + length);

					String columnQualifier = Bytes.toString(slice);
					if (columnQualifier.equals("v")) {
						float volume = Bytes.toFloat(entry.getValue());
						data.add(timestamp, volume);
						if (startDate.getMillis() > timestamp) {
							// Fetch just the one measurement after the end of
							// the interval
							isScanCompleted = true;
							break;
						}
					}
				}
				if (isScanCompleted) {
					break;
				}
			}
			scanner.close();
			table.close();
			connection.close();

			Collections.sort(data.getValues(),
					new Comparator<SmartMeterDataPoint>() {

						public int compare(SmartMeterDataPoint o1,
								SmartMeterDataPoint o2) {
							if (o1.timestamp <= o2.timestamp) {
								return -1;
							} else {
								return 1;
							}
						}
					});

			return data;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}

	public ShowerCollectionResult query(ShowerCollectionQuery query) {
		ShowerCollectionResult data = new ShowerCollectionResult();

		DateTime startDate = new DateTime(query.getStartDate());
		DateTime endDate = new DateTime(query.getEndDate());

		switch (query.getGranularity()) {
		case TemporalConstants.DAILY:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
					0, 0);
			endDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
					0, 0).plusDays(1);
			break;
		case TemporalConstants.WEEKLY:
			DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
			DateTime sunday = startDate.withDayOfWeek(DateTimeConstants.SUNDAY);
			startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(),
					monday.getDayOfMonth(), 0, 0, 0);
			endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(),
					sunday.getDayOfMonth(), 0, 0, 0).plusDays(1);
			break;
		case TemporalConstants.MONTHLY:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), 1, 0, 0, 0);
			endDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.dayOfMonth()
							.getMaximumValue(), 0, 0, 0).plusDays(1);
			;
			break;
		}

		DateTime timeThreshold = (new DateTime()).plusDays(1);
		long timestampThreshold = timeThreshold.getMillis();
		if (timestampThreshold < endDate.getMillis()) {
			endDate = timeThreshold;
		}

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.amphiroTableName));
			byte[] columnFamily = Bytes.toBytes("m");

			byte[] applicationKey = query.getApplicationKey().toString()
					.getBytes("UTF-8");
			byte[] applicationKeyHash = md.digest(applicationKey);

			byte[] deviceId = query.getDeviceId().toString().getBytes("UTF-8");
			byte[] deviceIdHash = md.digest(deviceId);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getRowKey(applicationKeyHash, deviceIdHash,
					startDate));
			scan.setStopRow(this.getRowKey(applicationKeyHash, deviceIdHash,
					endDate));

			ResultScanner scanner = table.getScanner(scan);

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(),
						32, 40));

				short offset = -1;
				DataPoint point = null;

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					short entryOffset = Bytes.toShort(Arrays.copyOfRange(
							entry.getKey(), 0, 2));

					if (offset != entryOffset) {
						if ((point != null)
								&& (point.timestamp <= timestampThreshold)) {
							data.add(point);
						}
						offset = entryOffset;
						point = new DataPoint();
						point.timestamp = (timeBucket + (long) offset) * 1000L;
					}

					int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
					byte[] slice = Arrays.copyOfRange(entry.getKey(), 3,
							3 + length);
					String columnQualifier = Bytes.toString(slice);
					if (columnQualifier.equals("si")) {
						point.showerId = Bytes.toLong(entry.getValue());
					} else if (columnQualifier.equals("st")) {
						point.showerTime = Bytes.toInt(entry.getValue());
					} else if (columnQualifier.equals("v")) {
						point.volume = Bytes.toFloat(entry.getValue());
					} else if (columnQualifier.equals("e")) {
						point.energy = Bytes.toFloat(entry.getValue());
					} else if (columnQualifier.equals("t")) {
						point.temperature = Bytes.toFloat(entry.getValue());
					}
				}
				if ((point != null) && (point.timestamp <= timestampThreshold)) {
					data.add(point);
				}
			}
			scanner.close();
			table.close();
			connection.close();

			Collections.sort(data.getShowers(), new Comparator<Shower>() {
				public int compare(Shower o1, Shower o2) {
					if (o1.getTimestamp() <= o2.getTimestamp()) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			return data;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}

	public ShowerResult query(ShowerQuery query) {
		ShowerResult data = new ShowerResult();

		DateTime startDate = new DateTime(query.getStartDate());
		DateTime endDate = new DateTime(query.getEndDate()).plusDays(1);

		startDate = new DateTime(startDate.getYear(),
				startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0, 0, 0);
		endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
				endDate.getDayOfMonth(), 0, 0, 0).plusDays(1);

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.amphiroTableName));
			byte[] columnFamily = Bytes.toBytes("m");

			byte[] applicationKey = query.getApplicationKey().toString()
					.getBytes("UTF-8");
			byte[] applicationKeyHash = md.digest(applicationKey);

			byte[] deviceId = query.getDeviceId().toString().getBytes("UTF-8");
			byte[] deviceIdHash = md.digest(deviceId);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getRowKey(applicationKeyHash, deviceIdHash,
					startDate));
			scan.setStopRow(this.getRowKey(applicationKeyHash, deviceIdHash,
					endDate));

			ResultScanner scanner = table.getScanner(scan);

			ShowerDetails shower = null;

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(),
						32, 40));

				short offset = -1;

				DataPoint point = null;

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					short entryOffset = Bytes.toShort(Arrays.copyOfRange(
							entry.getKey(), 0, 2));

					if (offset != entryOffset) {
						if ((point != null)
								&& (point.showerId == query.getShowerId())) {
							if (shower == null) {
								shower = new ShowerDetails(point.showerId);
							}
							shower.add(point);
						}
						offset = entryOffset;
						point = new DataPoint();
						point.timestamp = (timeBucket + (long) offset) * 1000L;
					}

					int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
					byte[] slice = Arrays.copyOfRange(entry.getKey(), 3,
							3 + length);
					String columnQualifier = Bytes.toString(slice);
					if (columnQualifier.equals("si")) {
						point.showerId = Bytes.toLong(entry.getValue());
					} else if (columnQualifier.equals("st")) {
						point.showerTime = Bytes.toInt(entry.getValue());
					} else if (columnQualifier.equals("v")) {
						point.volume = Bytes.toFloat(entry.getValue());
					} else if (columnQualifier.equals("e")) {
						point.energy = Bytes.toFloat(entry.getValue());
					} else if (columnQualifier.equals("t")) {
						point.temperature = Bytes.toFloat(entry.getValue());
					}
				}
				if ((point != null) && (point.showerId == query.getShowerId())) {
					if (shower == null) {
						shower = new ShowerDetails(point.showerId);
					}
					shower.add(point);
				}
			}

			Collections.sort(shower.getMeasurements(),
					new Comparator<DataPoint>() {

						public int compare(DataPoint o1, DataPoint o2) {
							if (o1.showerTime <= o2.showerTime) {
								return -1;
							} else {
								return 1;
							}
						}
					});

			scanner.close();
			table.close();
			connection.close();

			data.setShower(shower);

			return data;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}
}