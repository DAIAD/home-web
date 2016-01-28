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
import java.util.UUID;

import org.apache.commons.logging.Log;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.ApplicationUser;
import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.amphiro.AmphiroAbstractDataPoint;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroDataPoint;
import eu.daiad.web.model.amphiro.AmphiroDataSeries;
import eu.daiad.web.model.amphiro.AmphiroMeasurement;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSession;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionDetails;
import eu.daiad.web.model.amphiro.AmphiroSessionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionQueryResult;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.export.ExportDataRequest;
import eu.daiad.web.model.export.ExtendedSessionData;

@Repository()
@Scope("prototype")
@PropertySource("${hbase.properties}")
public class HBaseAmphiroMeasurementRepository implements
		IAmphiroMeasurementRepository {

	private enum EnumTimeInterval {
		UNDEFINED(0), HOUR(3600), DAY(86400);

		private final int value;

		private EnumTimeInterval(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	private String quorum;

	@Value("${hbase.data.time.partitions}")
	private short timePartitions;

	private String amphiroTableMeasurements = "daiad:amphiro-measurements";

	private String amphiroTableSessionByTime = "daiad:amphiro-sessions-by-time";

	private String amphiroTableSessionByUser = "daiad:amphiro-sessions-by-user";

	private String columnFamilyName = "cf";

	private static final Log logger = LogFactory
			.getLog(HBaseAmphiroMeasurementRepository.class);

	@Autowired
	public HBaseAmphiroMeasurementRepository(
			@Value("${hbase.zookeeper.quorum}") String quorum) {
		this.quorum = quorum;
	}

	@Override
	public List<ExtendedSessionData> exportSessions(ExportDataRequest data)
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
						.valueOf(this.amphiroTableSessionByTime));
				byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

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
							long sessionId = Bytes.toLong(Arrays.copyOfRange(
									r.getRow(), 42, 50));

							session.setId(sessionId);

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

									session.setTimestamp((timeBucket + offset) * 1000L);
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

	private void storeSessionByUser(Connection connection,
			ApplicationUser user, AmphiroDevice device,
			AmphiroMeasurementCollection data) throws Exception {
		Table table = null;
		try {
			if ((data == null) || (data.getSessions() == null)) {
				return;
			}
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName
					.valueOf(this.amphiroTableSessionByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int i = 0; i < data.getSessions().size(); i++) {
				AmphiroSession s = data.getSessions().get(i);

				byte[] userKey = data.getUserKey().toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKey);

				byte[] deviceKey = data.getDeviceKey().toString()
						.getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				long timestamp = s.getTimestamp() / 1000;
				long offset = timestamp % 86400;
				long timeBucket = timestamp - offset;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] sessionIdBytes = Bytes.toBytes(s.getId());

				byte[] rowKey = new byte[userKeyHash.length
						+ deviceKeyHash.length + timeBucketBytes.length
						+ sessionIdBytes.length];

				System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length,
						deviceKeyHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey,
						(userKeyHash.length + deviceKeyHash.length),
						timeBucketBytes.length);
				System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length
						+ deviceKeyHash.length + timeBucketBytes.length),
						sessionIdBytes.length);

				Put put = new Put(rowKey);
				byte[] column;

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

	private void storeSessionByTime(Connection connection,
			ApplicationUser user, AmphiroDevice device,
			AmphiroMeasurementCollection data) throws Exception {
		Table table = null;
		try {
			if ((data == null) || (data.getSessions() == null)) {
				return;
			}
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName
					.valueOf(this.amphiroTableSessionByTime));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int i = 0; i < data.getSessions().size(); i++) {
				AmphiroSession s = data.getSessions().get(i);

				short partition = (short) (s.getTimestamp() % this.timePartitions);
				byte[] partitionBytes = Bytes.toBytes(partition);

				byte[] userKey = data.getUserKey().toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKey);

				byte[] deviceKey = data.getDeviceKey().toString()
						.getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				long timestamp = s.getTimestamp() / 1000;
				long offset = timestamp % 86400;
				long timeBucket = timestamp - offset;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] sessionIdBytes = Bytes.toBytes(s.getId());

				byte[] rowKey = new byte[partitionBytes.length
						+ timeBucketBytes.length + userKeyHash.length
						+ deviceKeyHash.length + sessionIdBytes.length];

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
				System.arraycopy(sessionIdBytes, 0, rowKey,
						(partitionBytes.length + timeBucketBytes.length
								+ userKeyHash.length + deviceKeyHash.length),
						sessionIdBytes.length);

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
				put.addColumn(columnFamily, column, device.getMacAddress()
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

	private void preProcessMeasurements(
			ArrayList<AmphiroMeasurement> measurements) {
		if (measurements.size() > 1) {
			Collections.sort(measurements,
					new Comparator<AmphiroMeasurement>() {

						@Override
						public int compare(AmphiroMeasurement m1,
								AmphiroMeasurement m2) {
							if (m1.getSessionId() <= m2.getSessionId()) {
								if (m1.getIndex() <= m2.getIndex()) {
									return -1;
								} else {
									return 1;
								}
							} else {
								return 1;
							}
						}
					});

			for (int i = measurements.size() - 1; i > 0; i--) {
				if (measurements.get(i).getSessionId() == measurements.get(
						i - 1).getSessionId()) {
					// Set volume
					float diff = measurements.get(i).getVolume()
							- measurements.get(i - 1).getVolume();
					measurements.get(i).setVolume((float)Math.round(diff * 1000f) / 1000f);
					// Set energy
					diff = measurements.get(i).getEnergy()
							- measurements.get(i - 1).getEnergy();
					measurements.get(i).setEnergy((float)Math.round(diff * 1000f) / 1000f);
				}
			}
		}
	}

	private void storeMeasurements(Connection connection, ApplicationUser user,
			AmphiroDevice device, AmphiroMeasurementCollection data)
			throws Exception {
		Table table = null;
		try {
			if ((data == null) || (data.getMeasurements() == null)) {
				return;
			}
			this.preProcessMeasurements(data.getMeasurements());

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName
					.valueOf(this.amphiroTableMeasurements));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				AmphiroMeasurement m = data.getMeasurements().get(i);

				if (m.getVolume() <= 0) {
					continue;
				}

				byte[] userKey = data.getUserKey().toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKey);

				byte[] deviceKey = data.getDeviceKey().toString()
						.getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				m.setTimestamp(m.getTimestamp() / 1000);

				long timeSlice = m.getTimestamp() % 3600;
				byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);
				if (timeSliceBytes.length != 2) {
					throw new RuntimeException("Invalid byte array length!");
				}

				long timeBucket = m.getTimestamp() - timeSlice;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] rowKey = new byte[userKeyHash.length
						+ deviceKeyHash.length + timeBucketBytes.length];
				System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length,
						deviceKeyHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey,
						(userKeyHash.length + deviceKeyHash.length),
						timeBucketBytes.length);

				Put p = new Put(rowKey);

				byte[] column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("s")));
				p.addColumn(columnFamily, column,
						Bytes.toBytes(m.getSessionId()));

				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("i")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getIndex()));
				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));
				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("t")));
				p.addColumn(columnFamily, column,
						Bytes.toBytes(m.getTemperature()));
				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("e")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getEnergy()));
				column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("h")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.isHistory()));

				table.put(p);
			}
		} finally {
			if (table != null) {
				table.close();
			}
		}
	}

	@Override
	public void storeData(ApplicationUser user, AmphiroDevice device,
			AmphiroMeasurementCollection data) {
		Connection connection = null;
		try {
			if (data == null) {
				return;
			}
			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			this.storeSessionByUser(connection, user, device, data);
			this.storeSessionByTime(connection, user, device, data);

			this.storeMeasurements(connection, user, device, data);

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

	private byte[] getUserDeviceHourRowKey(byte[] userKeyHash,
			byte[] deviceKeyHash, DateTime date) throws Exception {
		return this.getUserDeviceTimeRowKey(userKeyHash, deviceKeyHash,
				date.getMillis(), EnumTimeInterval.HOUR);
	}

	private byte[] getUserDeviceDayRowKey(byte[] userKeyHash,
			byte[] deviceKeyHash, DateTime date) throws Exception {
		return this.getUserDeviceTimeRowKey(userKeyHash, deviceKeyHash,
				date.getMillis(), EnumTimeInterval.DAY);
	}

	private byte[] getUserDeviceTimeRowKey(byte[] userKeyHash,
			byte[] deviceKeyHash, long date, EnumTimeInterval interval)
			throws Exception {

		long intervalInSeconds = EnumTimeInterval.HOUR.getValue();
		switch (interval) {
		case HOUR:
			intervalInSeconds = interval.getValue();
			break;
		case DAY:
			intervalInSeconds = interval.getValue();
			break;
		default:
			throw new RuntimeException(
					String.format("Time interval [%s] is not supported.",
							interval.toString()));
		}

		long timestamp = date / 1000;
		long timeSlice = timestamp % intervalInSeconds;
		long timeBucket = timestamp - timeSlice;
		byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

		byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + 8];
		System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
		System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length,
				deviceKeyHash.length);
		System.arraycopy(timeBucketBytes, 0, rowKey,
				(deviceKeyHash.length + deviceKeyHash.length),
				timeBucketBytes.length);

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

	@Override
	public AmphiroMeasurementQueryResult searchMeasurements(
			AmphiroMeasurementQuery query) {
		AmphiroMeasurementQueryResult data = new AmphiroMeasurementQueryResult();

		DateTime startDate = new DateTime(query.getStartDate());
		DateTime endDate = new DateTime(query.getEndDate());

		switch (query.getGranularity()) {
		case TemporalConstants.NONE:
			// Retrieve values at the highest granularity, that is at the
			// measurement level
			break;
		case TemporalConstants.HOUR:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(),
					startDate.getHourOfDay(), 0, 0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
					endDate.getDayOfMonth(), endDate.getHourOfDay(), 59, 59);
			break;
		case TemporalConstants.DAY:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
					0, 0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
					endDate.getDayOfMonth(), 23, 59, 59);
			break;
		case TemporalConstants.WEEK:
			DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
			DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
			startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(),
					monday.getDayOfMonth(), 0, 0, 0);
			endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(),
					sunday.getDayOfMonth(), 23, 59, 59);
			break;
		case TemporalConstants.MONTH:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), 1, 0, 0, 0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
					endDate.dayOfMonth().getMaximumValue(), 23, 59, 59);
			break;
		case TemporalConstants.YEAR:
			startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0);
			endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59);
			break;
		default:
			return new AmphiroMeasurementQueryResult(-1,
					"Granularity level not supported.");
		}

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.amphiroTableMeasurements));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			UUID deviceKeys[] = query.getDeviceKey();

			for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
				byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes(
						"UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				Scan scan = new Scan();
				scan.addFamily(columnFamily);
				scan.setStartRow(this.getUserDeviceHourRowKey(userKeyHash,
						deviceKeyHash, startDate));
				scan.setStopRow(this.getUserDeviceHourRowKey(userKeyHash,
						deviceKeyHash, endDate));

				ResultScanner scanner = table.getScanner(scan);

				AmphiroDataSeries series = new AmphiroDataSeries(
						deviceKeys[deviceIndex], query.getGranularity());

				data.getSeries().add(series);

				ArrayList<AmphiroDataPoint> points = new ArrayList<AmphiroDataPoint>();

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					NavigableMap<byte[], byte[]> map = r
							.getFamilyMap(columnFamily);

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(
							r.getRow(), 32, 40));

					short offset = -1;
					AmphiroDataPoint point = null;

					for (Entry<byte[], byte[]> entry : map.entrySet()) {
						short entryOffset = Bytes.toShort(Arrays.copyOfRange(
								entry.getKey(), 0, 2));

						if (offset != entryOffset) {
							if (point != null) {
								points.add(point);
							}
							offset = entryOffset;
							point = new AmphiroDataPoint();
							point.setTimestamp((timeBucket + offset) * 1000L);
						}

						int length = Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
						byte[] slice = Arrays.copyOfRange(entry.getKey(), 3,
								3 + length);
						String qualifier = Bytes.toString(slice);

						switch (qualifier) {
						case "h":
							point.setHistory(Bytes.toBoolean(entry.getValue()));
							break;
						case "v":
							point.setVolume(Bytes.toFloat(entry.getValue()));
							break;
						case "e":
							point.setEnergy(Bytes.toFloat(entry.getValue()));
							break;
						case "t":
							point.setTemperature(Bytes.toFloat(entry.getValue()));
							break;
						case "s":
							point.setSessionId(Bytes.toLong(entry.getValue()));
							break;
						case "i":
							point.setIndex(Bytes.toInt(entry.getValue()));
							break;
						}
					}
					if (point != null) {
						points.add(point);
					}
				}
				scanner.close();

				series.setPoints(points);

				Collections.sort(series.getPoints(),
						new Comparator<AmphiroAbstractDataPoint>() {

							@Override
							public int compare(AmphiroAbstractDataPoint o1,
									AmphiroAbstractDataPoint o2) {
								if (o1.getTimestamp() <= o2.getTimestamp()) {
									return -1;
								} else {
									return 1;
								}
							}
						});
			}

			table.close();
			connection.close();

			return data;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}

	@Override
	public AmphiroSessionCollectionQueryResult searchSessions(
			AmphiroSessionCollectionQuery query) {
		AmphiroSessionCollectionQueryResult data = new AmphiroSessionCollectionQueryResult();

		DateTime startDate = new DateTime(query.getStartDate());
		DateTime endDate = new DateTime(query.getEndDate());

		switch (query.getGranularity()) {
		case TemporalConstants.NONE:
			// Retrieve values at the highest granularity, that is at the
			// measurement level
			break;
		case TemporalConstants.HOUR:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(),
					startDate.getHourOfDay(), 0, 0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
					endDate.getDayOfMonth(), endDate.getHourOfDay(), 59, 59);
			break;
		case TemporalConstants.DAY:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
					0, 0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
					endDate.getDayOfMonth(), 23, 59, 59);
			break;
		case TemporalConstants.WEEK:
			DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
			DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
			startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(),
					monday.getDayOfMonth(), 0, 0, 0);
			endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(),
					sunday.getDayOfMonth(), 23, 59, 59);
			break;
		case TemporalConstants.MONTH:
			startDate = new DateTime(startDate.getYear(),
					startDate.getMonthOfYear(), 1, 0, 0, 0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
					endDate.dayOfMonth().getMaximumValue(), 23, 59, 59);
			break;
		case TemporalConstants.YEAR:
			startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0);
			endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59);
			break;
		default:
			return new AmphiroSessionCollectionQueryResult(-1,
					"Granularity level not supported.");
		}

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.amphiroTableSessionByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			UUID deviceKeys[] = query.getDeviceKey();

			for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
				ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();

				byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes(
						"UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				Scan scan = new Scan();
				scan.addFamily(columnFamily);
				scan.setStartRow(this.getUserDeviceDayRowKey(userKeyHash,
						deviceKeyHash, startDate));
				scan.setStopRow(this.getUserDeviceDayRowKey(userKeyHash,
						deviceKeyHash, endDate));

				ResultScanner scanner = table.getScanner(scan);

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					NavigableMap<byte[], byte[]> map = r
							.getFamilyMap(columnFamily);

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(
							r.getRow(), 32, 40));

					AmphiroSession session = new AmphiroSession();
					session.setId(Bytes.toLong(Arrays.copyOfRange(r.getRow(),
							40, 48)));

					for (Entry<byte[], byte[]> entry : map.entrySet()) {

						String qualifier = Bytes.toString(entry.getKey());

						switch (qualifier) {
						case "m:offset":
							int offset = Bytes.toInt(entry.getValue());
							session.setTimestamp((timeBucket + offset) * 1000L);
							break;
						case "m:t":
							session.setTemperature(Bytes.toFloat(entry
									.getValue()));
							break;
						case "m:v":
							session.setVolume(Bytes.toFloat(entry.getValue()));
							break;
						case "m:f":
							session.setFlow(Bytes.toFloat(entry.getValue()));
							break;
						case "m:e":
							session.setEnergy(Bytes.toFloat(entry.getValue()));
							break;
						case "m:d":
							session.setDuration(Bytes.toInt(entry.getValue()));
							break;
						case "r:h":
							session.setHistory(Bytes.toBoolean(entry.getValue()));
							break;
						default:
							session.addProperty(qualifier,
									new String(entry.getValue(),
											StandardCharsets.UTF_8));
							break;
						}
					}

					if ((session.getTimestamp() >= startDate.getMillis())
							&& (session.getTimestamp() <= endDate.getMillis())) {
						sessions.add(session);
					}
				}

				scanner.close();

				AmphiroSessionCollection collection = new AmphiroSessionCollection(
						deviceKeys[deviceIndex], query.getGranularity());

				collection.addSessions(sessions);

				if (collection.getSessions().size() > 0) {
					Collections.sort(collection.getSessions(),
							new Comparator<AmphiroAbstractSession>() {
								@Override
								public int compare(AmphiroAbstractSession o1,
										AmphiroAbstractSession o2) {
									if (o1.getTimestamp() <= o2.getTimestamp()) {
										return -1;
									} else {
										return 1;
									}
								}
							});
				}

				data.getDevices().add(collection);
			}

			table.close();
			connection.close();

			return data;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}

	@Override
	public AmphiroSessionQueryResult getSession(AmphiroSessionQuery query) {
		AmphiroSessionQueryResult data = new AmphiroSessionQueryResult();

		// Add temporal buffer
		DateTime startDate = new DateTime(query.getStartDate()).minusHours(1);
		DateTime endDate = new DateTime(query.getEndDate()).plusHours(1);

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.amphiroTableSessionByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			byte[] deviceKey = query.getDeviceKey().toString()
					.getBytes("UTF-8");
			byte[] deviceKeyHash = md.digest(deviceKey);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getUserDeviceDayRowKey(userKeyHash,
					deviceKeyHash, startDate));
			scan.setStopRow(this.getUserDeviceDayRowKey(userKeyHash,
					deviceKeyHash, endDate));

			ResultScanner scanner = table.getScanner(scan);

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(),
						32, 40));

				AmphiroSessionDetails session = new AmphiroSessionDetails();
				session.setId(Bytes.toLong(Arrays.copyOfRange(r.getRow(), 40,
						48)));

				for (Entry<byte[], byte[]> entry : map.entrySet()) {

					String qualifier = Bytes.toString(entry.getKey());

					switch (qualifier) {
					case "m:offset":
						int offset = Bytes.toInt(entry.getValue());
						session.setTimestamp((timeBucket + offset) * 1000L);
						break;
					case "m:t":
						session.setTemperature(Bytes.toFloat(entry.getValue()));
						break;
					case "m:v":
						session.setVolume(Bytes.toFloat(entry.getValue()));
						break;
					case "m:f":
						session.setFlow(Bytes.toFloat(entry.getValue()));
						break;
					case "m:e":
						session.setEnergy(Bytes.toFloat(entry.getValue()));
						break;
					case "m:d":
						session.setDuration(Bytes.toInt(entry.getValue()));
						break;
					case "r:h":
						session.setHistory(Bytes.toBoolean(entry.getValue()));
						break;
					default:
						session.addProperty(qualifier,
								new String(entry.getValue(),
										StandardCharsets.UTF_8));
						break;
					}
				}

				if ((session.getTimestamp() >= startDate.getMillis())
						&& (session.getTimestamp() <= endDate.getMillis())
						&& (session.getId() == query.getSessionId())) {

					session.setMeasurements(this.getSessionMeasurements(query,
							connection));

					data.setSession(session);
					break;
				}
			}

			scanner.close();
			table.close();
			connection.close();

			return data;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}

	private ArrayList<AmphiroMeasurement> getSessionMeasurements(
			AmphiroSessionQuery query, Connection connection) {
		ArrayList<AmphiroMeasurement> measurements = new ArrayList<AmphiroMeasurement>();

		DateTime startDate = new DateTime(query.getStartDate());
		DateTime endDate = new DateTime(query.getEndDate()).plusDays(1);

		startDate = new DateTime(startDate.getYear(),
				startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0, 0, 0);
		endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(),
				endDate.getDayOfMonth(), 0, 0, 0).plusDays(1);

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.amphiroTableMeasurements));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyKey = md.digest(userKey);

			byte[] deviceKey = query.getDeviceKey().toString()
					.getBytes("UTF-8");
			byte[] deviceKeyHash = md.digest(deviceKey);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getUserDeviceHourRowKey(userKeyKey,
					deviceKeyHash, startDate));
			scan.setStopRow(this.getUserDeviceHourRowKey(userKeyKey,
					deviceKeyHash, endDate));

			ResultScanner scanner = table.getScanner(scan);

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(),
						32, 40));

				short offset = -1;

				AmphiroMeasurement measurement = null;

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					short entryOffset = Bytes.toShort(Arrays.copyOfRange(
							entry.getKey(), 0, 2));

					if (offset != entryOffset) {
						if ((measurement != null)
								&& (measurement.getSessionId() == query
										.getSessionId())) {
							measurements.add(measurement);
						}
						offset = entryOffset;
						measurement = new AmphiroMeasurement();
						measurement.setTimestamp((timeBucket + offset) * 1000L);
					}

					int length = Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
					byte[] slice = Arrays.copyOfRange(entry.getKey(), 3,
							3 + length);

					String qualifier = Bytes.toString(slice);

					switch (qualifier) {
					case "h":
						measurement
								.setHistory(Bytes.toBoolean(entry.getValue()));
						break;
					case "v":
						measurement.setVolume(Bytes.toFloat(entry.getValue()));
						break;
					case "e":
						measurement.setEnergy(Bytes.toFloat(entry.getValue()));
						break;
					case "t":
						measurement.setTemperature(Bytes.toFloat(entry
								.getValue()));
						break;
					case "s":
						measurement
								.setSessionId(Bytes.toLong(entry.getValue()));
						break;
					case "i":
						measurement.setIndex(Bytes.toInt(entry.getValue()));
						break;
					}
				}
				if ((measurement != null)
						&& (measurement.getSessionId() == query.getSessionId())) {
					measurements.add(measurement);
				}
			}

			scanner.close();
			table.close();

			return measurements;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.", ex);
		}

		return null;
	}

}
