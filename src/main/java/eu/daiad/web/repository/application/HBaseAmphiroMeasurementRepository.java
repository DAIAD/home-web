package eu.daiad.web.repository.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.amphiro.AmphiroAbstractDataPoint;
import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
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
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DataErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.query.AmphiroDataPoint;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.GroupDataSeries;

@Repository()
@Scope("prototype")
public class HBaseAmphiroMeasurementRepository implements IAmphiroMeasurementRepository {

	private static final Log logger = LogFactory.getLog(HBaseAmphiroMeasurementRepository.class);

	private final String ERROR_RELEASE_RESOURCES = "Failed to release resources";

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

	@Value("${hbase.data.time.partitions}")
	private short timePartitions;

	@Value("${scanner-cache-size}")
	private int scanCacheSize = 1;

	private String amphiroTableMeasurements = "daiad:amphiro-measurements";

	private String amphiroTableSessionByTime = "daiad:amphiro-sessions-by-time";

	private String amphiroTableSessionByUser = "daiad:amphiro-sessions-by-user";

	private String amphiroTableSessionIndex = "daiad:amphiro-sessions-index";

	private String columnFamilyName = "cf";

	@Autowired
	private HBaseConfigurationBuilder configurationBuilder;

	private Connection connection = null;

	@Override
	public void open() throws IOException {
		if (this.connection == null) {
			Configuration config = this.configurationBuilder.build();
			this.connection = ConnectionFactory.createConnection(config);
		}
	}

	@Override
	public boolean isOpen() {
		return ((this.connection != null) && (!this.connection.isClosed()));
	}

	@Override
	public void close() {
		try {
			if ((this.connection != null) && (!this.connection.isClosed())) {
				this.connection.close();
				this.connection = null;
			}
		} catch (Exception ex) {
			logger.error(ERROR_RELEASE_RESOURCES, ex);
		}
	}

	private void updateSessionIndex(Connection connection, UUID userKey, AmphiroMeasurementCollection data)
					throws Exception {
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.amphiroTableSessionIndex));

			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);
			byte[] columnQualifier = Bytes.toBytes("ts");

			for (int i = data.getSessions().size() - 1; i >= 0; i--) {
				AmphiroSession s = data.getSessions().get(i);

				byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKeyBytes);

				byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				byte[] sessionIdBytes = Bytes.toBytes(s.getId());

				byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + sessionIdBytes.length];

				System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
				System.arraycopy(sessionIdBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
								sessionIdBytes.length);

				Get get = new Get(rowKey);

				boolean isDelete = (!data.getSessions().get(i).isHistory())
								&& (data.getSessions().get(i).getDelete() != null);

				if ((table.get(get).getRow() == null) || (isDelete)) {
					Put put = new Put(rowKey);
					put.addColumn(columnFamily, columnQualifier, Bytes.toBytes(s.getTimestamp()));

					table.put(put);
				} else {
					data.getSessions().remove(i);
					for (int j = data.getMeasurements().size() - 1; j >= 0; j--) {
						if (data.getMeasurements().get(j).getSessionId() == s.getId()) {
							data.getMeasurements().remove(j);
						}
					}
				}
			}
		} finally {
			try {
				if (table != null) {
					table.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	private void storeSessionByUser(Connection connection, UUID userKey, AmphiroMeasurementCollection data)
					throws Exception {
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.amphiroTableSessionByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int i = 0; i < data.getSessions().size(); i++) {
				AmphiroSession s = data.getSessions().get(i);

				long timestamp, timeBucket, offset;

				byte[] timeBucketBytes, rowKey;

				byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKeyBytes);

				byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				byte[] sessionIdBytes = Bytes.toBytes(s.getId());

				// Delete existing record
				if ((!s.isHistory()) && (s.getDelete() != null)) {
					timestamp = s.getDelete().getTimestamp() / 1000;
					offset = timestamp % EnumTimeInterval.DAY.getValue();

					timeBucket = timestamp - offset;
					timeBucketBytes = Bytes.toBytes(timeBucket);

					rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length
									+ sessionIdBytes.length];

					System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
					System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
					System.arraycopy(timeBucketBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
									timeBucketBytes.length);
					System.arraycopy(sessionIdBytes, 0, rowKey,
									(userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length),
									sessionIdBytes.length);

					Delete delete = new Delete(rowKey);
					table.delete(delete);
				}

				// Insert record
				timestamp = s.getTimestamp() / 1000;
				offset = timestamp % EnumTimeInterval.DAY.getValue();
				timeBucket = timestamp - offset;

				timeBucketBytes = Bytes.toBytes(timeBucket);

				rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length
								+ sessionIdBytes.length];

				System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
								timeBucketBytes.length);
				System.arraycopy(sessionIdBytes, 0, rowKey,
								(userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length),
								sessionIdBytes.length);

				Put put = new Put(rowKey);
				byte[] column;

				column = Bytes.toBytes("m:offset");
				put.addColumn(columnFamily, column, Bytes.toBytes((int) offset));

				column = Bytes.toBytes("m:t");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

				column = Bytes.toBytes("m:v");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

				column = Bytes.toBytes("m:f");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

				column = Bytes.toBytes("m:e");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

				column = Bytes.toBytes("m:d");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

				column = Bytes.toBytes("r:h");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.isHistory()));

				for (int p = 0, count = s.getProperties().size(); p < count; p++) {
					column = Bytes.toBytes(s.getProperties().get(p).getKey());
					put.addColumn(columnFamily, column,
									s.getProperties().get(p).getValue().getBytes(StandardCharsets.UTF_8));
				}

				table.put(put);
			}
		} finally {
			try {
				if (table != null) {
					table.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	private void storeSessionByTime(Connection connection, UUID userKey, AmphiroMeasurementCollection data)
					throws Exception {
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.amphiroTableSessionByTime));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int i = 0; i < data.getSessions().size(); i++) {
				AmphiroSession s = data.getSessions().get(i);

				long timestamp, offset, timeBucket;

				byte[] partitionBytes, timeBucketBytes, rowKey;

				byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKeyBytes);

				byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				byte[] sessionIdBytes = Bytes.toBytes(s.getId());

				// Delete existing record
				if ((!s.isHistory()) && (s.getDelete() != null)) {
					for (short partitionIndex = 0; partitionIndex < this.timePartitions; partitionIndex++) {
						partitionBytes = Bytes.toBytes(partitionIndex);

						timestamp = s.getDelete().getTimestamp() / 1000;
						offset = timestamp % EnumTimeInterval.DAY.getValue();

						timeBucket = timestamp - offset;
						timeBucketBytes = Bytes.toBytes(timeBucket);

						rowKey = new byte[partitionBytes.length + timeBucketBytes.length + userKeyHash.length
										+ deviceKeyHash.length + sessionIdBytes.length];

						System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
						System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);
						System.arraycopy(userKeyHash, 0, rowKey, (partitionBytes.length + timeBucketBytes.length),
										userKeyHash.length);
						System.arraycopy(deviceKeyHash, 0, rowKey,
										(partitionBytes.length + timeBucketBytes.length + userKeyHash.length),
										deviceKeyHash.length);
						System.arraycopy(sessionIdBytes, 0, rowKey, (partitionBytes.length + timeBucketBytes.length
										+ userKeyHash.length + deviceKeyHash.length), sessionIdBytes.length);

						Delete delete = new Delete(rowKey);
						table.delete(delete);
					}
				}

				// Insert new record

				short partition = (short) (s.getTimestamp() % this.timePartitions);
				partitionBytes = Bytes.toBytes(partition);

				timestamp = s.getTimestamp() / 1000;
				offset = timestamp % EnumTimeInterval.DAY.getValue();
				timeBucket = timestamp - offset;

				timeBucketBytes = Bytes.toBytes(timeBucket);

				rowKey = new byte[partitionBytes.length + timeBucketBytes.length + userKeyHash.length
								+ deviceKeyHash.length + sessionIdBytes.length];

				System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
				System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);
				System.arraycopy(userKeyHash, 0, rowKey, (partitionBytes.length + timeBucketBytes.length),
								userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey,
								(partitionBytes.length + timeBucketBytes.length + userKeyHash.length),
								deviceKeyHash.length);
				System.arraycopy(sessionIdBytes, 0, rowKey, (partitionBytes.length + timeBucketBytes.length
								+ userKeyHash.length + deviceKeyHash.length), sessionIdBytes.length);

				Put put = new Put(rowKey);

				byte[] column;

				column = Bytes.toBytes("m:offset");
				put.addColumn(columnFamily, column, Bytes.toBytes((int) offset));

				column = Bytes.toBytes("m:t");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getTemperature()));

				column = Bytes.toBytes("m:v");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getVolume()));

				column = Bytes.toBytes("m:f");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getFlow()));

				column = Bytes.toBytes("m:e");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getEnergy()));

				column = Bytes.toBytes("m:d");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.getDuration()));

				column = Bytes.toBytes("r:h");
				put.addColumn(columnFamily, column, Bytes.toBytes(s.isHistory()));

				for (int p = 0, count = s.getProperties().size(); p < count; p++) {
					column = Bytes.toBytes(s.getProperties().get(p).getKey());
					put.addColumn(columnFamily, column,
									s.getProperties().get(p).getValue().getBytes(StandardCharsets.UTF_8));
				}

				table.put(put);
			}
		} finally {
			try {
				if (table != null) {
					table.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	private void preProcessData(AmphiroMeasurementCollection data) {
		// Sort sessions
		ArrayList<AmphiroSession> sessions = data.getSessions();

		Collections.sort(sessions, new Comparator<AmphiroSession>() {

			@Override
			public int compare(AmphiroSession s1, AmphiroSession s2) {
				if (s1.getId() == s2.getId()) {
					throw new RuntimeException("Session id must be unique.");
				} else if (s1.getId() < s2.getId()) {
					return -1;
				} else {
					return 1;
				}
			}
		});

		// Check if historical data require a delete operation
		for (AmphiroSession s : sessions) {
			if ((s.isHistory()) && (s.getDelete() != null)) {
				throw new ApplicationException(DataErrorCode.DELETE_NOT_ALLOWED_FOR_HISTORY).set("session", s.getId());
			}
		}

		// Sort measurements
		ArrayList<AmphiroMeasurement> measurements = data.getMeasurements();

		if ((measurements != null) && (measurements.size() > 0)) {

			Collections.sort(measurements, new Comparator<AmphiroMeasurement>() {

				@Override
				public int compare(AmphiroMeasurement m1, AmphiroMeasurement m2) {
					if (m1.getSessionId() == m2.getSessionId()) {
						if (m1.getIndex() == m2.getIndex()) {
							throw new RuntimeException("Session measurement indexes must be unique.");
						}
						if (m1.getTimestamp() == m2.getTimestamp()) {
							throw new RuntimeException("Session measurement timestamps must be unique.");
						}
						if (m1.getIndex() < m2.getIndex()) {
							if (m1.getTimestamp() > m2.getTimestamp()) {
								throw new RuntimeException(
												"Session measurements timestamp and index has ambiguous orderning.");
							}
							return -1;
						} else {
							if (m1.getTimestamp() < m2.getTimestamp()) {
								throw new RuntimeException(
												"Session measurements timestamp and index has ambiguous orderning.");
							}
							return 1;
						}
					} else if (m1.getSessionId() < m2.getSessionId()) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			// Compute difference for volume and energy
			for (int i = measurements.size() - 1; i > 0; i--) {
				if (measurements.get(i).getSessionId() == measurements.get(i - 1).getSessionId()) {
					// Set volume
					float diff = measurements.get(i).getVolume() - measurements.get(i - 1).getVolume();
					measurements.get(i).setVolume((float) Math.round(diff * 1000f) / 1000f);
					// Set energy
					diff = measurements.get(i).getEnergy() - measurements.get(i - 1).getEnergy();
					measurements.get(i).setEnergy((float) Math.round(diff * 1000f) / 1000f);
				}
			}

			// Set session for every measurement
			for (AmphiroMeasurement m : measurements) {
				for (AmphiroSession s : sessions) {
					if (m.getSessionId() == s.getId()) {
						if ((s.isHistory()) && (s.getTimestamp() != m.getTimestamp())) {
							throw new ApplicationException(DataErrorCode.HISTORY_SESSION_MEASUREMENT_TIMESTAMP_MISMATCH)
											.set("session", m.getSessionId()).set("index", m.getIndex());
						}

						m.setSession(s);
						break;
					}
				}
				if (m.getSession() == null) {
					throw new ApplicationException(DataErrorCode.NO_SESSION_FOUND_FOR_MEASUREMENT).set("session",
									m.getSessionId()).set("index", m.getIndex());
				}
			}
		}
	}

	private void storeMeasurements(Connection connection, UUID userKey, AmphiroMeasurementCollection data)
					throws Exception {
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.amphiroTableMeasurements));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				AmphiroMeasurement m = data.getMeasurements().get(i);

				byte[] userKeyBytes = userKey.toString().getBytes("UTF-8");
				byte[] userKeyHash = md.digest(userKeyBytes);

				byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				long timestamp, timeBucket, timeSlice;

				byte[] timeBucketBytes, timeSliceBytes, rowKey, column;

				AmphiroSession s = m.getSession();

				if ((!s.isHistory()) && (s.getDelete() != null) && (s.getTimestamp() != s.getDelete().getTimestamp())) {
					timestamp = s.getDelete().getTimestamp() / 1000;

					timeSlice = timestamp % EnumTimeInterval.HOUR.getValue();
					timeSliceBytes = Bytes.toBytes((short) timeSlice);

					timeBucket = timestamp - timeSlice;
					timeBucketBytes = Bytes.toBytes(timeBucket);

					rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length];
					System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
					System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
					System.arraycopy(timeBucketBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
									timeBucketBytes.length);

					Delete delete = new Delete(rowKey);

					column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("s")));
					delete.addColumns(columnFamily, column);

					column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("i")));
					delete.addColumns(columnFamily, column);

					column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
					delete.addColumns(columnFamily, column);

					column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("t")));
					delete.addColumns(columnFamily, column);

					column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("e")));
					delete.addColumns(columnFamily, column);

					column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("h")));
					delete.addColumns(columnFamily, column);

					table.delete(delete);
				}

				if (m.getVolume() <= 0) {
					continue;
				}

				timestamp = m.getTimestamp() / 1000;

				timeSlice = timestamp % EnumTimeInterval.HOUR.getValue();
				timeSliceBytes = Bytes.toBytes((short) timeSlice);

				timeBucket = timestamp - timeSlice;
				timeBucketBytes = Bytes.toBytes(timeBucket);

				rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length];
				System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
								timeBucketBytes.length);

				Put p = new Put(rowKey);

				column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("s")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getSessionId()));

				column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("i")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getIndex()));
				column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));
				column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("t")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getTemperature()));
				column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("e")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getEnergy()));
				column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("h")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.isHistory()));

				table.put(p);
			}
		} finally {
			try {
				if (table != null) {
					table.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	@Override
	public void storeData(UUID userKey, AmphiroMeasurementCollection data) throws ApplicationException {
		boolean autoClose = false;

		try {
			if ((data == null) || (data.getSessions() == null) || (data.getSessions().size() == 0)) {
				return;
			}

			if (!this.isOpen()) {
				this.open();
				autoClose = true;
			}

			this.preProcessData(data);

			this.updateSessionIndex(connection, userKey, data);

			if (data.getSessions().size() > 0) {
				this.storeSessionByUser(connection, userKey, data);
				this.storeSessionByTime(connection, userKey, data);

				if ((data.getMeasurements() != null) && (data.getMeasurements().size() > 0)) {
					this.storeMeasurements(connection, userKey, data);
				}
			}
		} catch (Exception ex) {
			autoClose = true;

			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			if (autoClose) {
				this.close();
			}
		}
	}

	private byte[] getUserDeviceHourRowKey(byte[] userKeyHash, byte[] deviceKeyHash, DateTime date) throws Exception {
		return this.getUserDeviceTimeRowKey(userKeyHash, deviceKeyHash, date.getMillis(), EnumTimeInterval.HOUR);
	}

	private byte[] getUserDeviceDayRowKey(byte[] userKeyHash, byte[] deviceKeyHash, DateTime date) throws Exception {
		return this.getUserDeviceTimeRowKey(userKeyHash, deviceKeyHash, date.getMillis(), EnumTimeInterval.DAY);
	}

	private byte[] getUserDeviceTimeRowKey(byte[] userKeyHash, byte[] deviceKeyHash, long date,
					EnumTimeInterval interval) throws Exception {

		long intervalInSeconds = EnumTimeInterval.HOUR.getValue();
		switch (interval) {
			case HOUR:
				intervalInSeconds = interval.getValue();
				break;
			case DAY:
				intervalInSeconds = interval.getValue();
				break;
			default:
				throw new RuntimeException(String.format("Time interval [%s] is not supported.", interval.toString()));
		}

		long timestamp = date / 1000;
		long timeSlice = timestamp % intervalInSeconds;
		long timeBucket = timestamp - timeSlice;
		byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

		byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length];
		System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
		System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
		System.arraycopy(timeBucketBytes, 0, rowKey, (deviceKeyHash.length + deviceKeyHash.length),
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
	public AmphiroMeasurementQueryResult searchMeasurements(DateTimeZone timezone, AmphiroMeasurementQuery query) {
		AmphiroMeasurementQueryResult data = new AmphiroMeasurementQueryResult();

		DateTime startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
		DateTime endDate = new DateTime(query.getEndDate(), DateTimeZone.UTC);

		switch (query.getGranularity()) {
			case TemporalConstants.NONE:
				// Retrieve values at the highest granularity, that is at the
				// measurement level
				break;
			case TemporalConstants.HOUR:
				startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(),
								startDate.getHourOfDay(), 0, 0, DateTimeZone.UTC);
				endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(),
								endDate.getHourOfDay(), 59, 59, DateTimeZone.UTC);
				break;
			case TemporalConstants.DAY:
				startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
								0, 0, DateTimeZone.UTC);
				endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23, 59,
								59, DateTimeZone.UTC);
				break;
			case TemporalConstants.WEEK:
				DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
				DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
				startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0, 0,
								DateTimeZone.UTC);
				endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59, 59,
								DateTimeZone.UTC);
				break;
			case TemporalConstants.MONTH:
				startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0, DateTimeZone.UTC);
				endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
								.getMaximumValue(), 23, 59, 59, DateTimeZone.UTC);
				break;
			case TemporalConstants.YEAR:
				startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
				endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, DateTimeZone.UTC);
				break;
			default:
				throw new ApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
								query.getGranularity());
		}

		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		try {
			Configuration config = this.configurationBuilder.build();

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.amphiroTableMeasurements));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			UUID deviceKeys[] = query.getDeviceKey();

			for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
				byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				Scan scan = new Scan();
				scan.addFamily(columnFamily);
				scan.setStartRow(this.getUserDeviceHourRowKey(userKeyHash, deviceKeyHash, startDate));
				scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getUserDeviceHourRowKey(userKeyHash,
								deviceKeyHash, endDate)));

				scanner = table.getScanner(scan);

				AmphiroDataSeries series = new AmphiroDataSeries(deviceKeys[deviceIndex], query.getGranularity());

				data.getSeries().add(series);

				ArrayList<eu.daiad.web.model.amphiro.AmphiroDataPoint> points = new ArrayList<eu.daiad.web.model.amphiro.AmphiroDataPoint>();

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

					short offset = -1;
					eu.daiad.web.model.amphiro.AmphiroDataPoint point = null;

					for (Entry<byte[], byte[]> entry : map.entrySet()) {
						short entryOffset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

						if (offset != entryOffset) {
							if ((point != null) && (point.getTimestamp() >= startDate.getMillis())
											&& (point.getTimestamp() <= endDate.getMillis())) {
								points.add(point);
							}
							offset = entryOffset;
							point = new eu.daiad.web.model.amphiro.AmphiroDataPoint();
							point.setTimestamp((timeBucket + offset) * 1000L);
						}

						int length = Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
						byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);
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
					if ((point != null) && (point.getTimestamp() >= startDate.getMillis())
									&& (point.getTimestamp() <= endDate.getMillis())) {
						points.add(point);
					}
				}
				scanner.close();
				scanner = null;

				series.setPoints(points, timezone);

				Collections.sort(series.getPoints(), new Comparator<AmphiroAbstractDataPoint>() {

					@Override
					public int compare(AmphiroAbstractDataPoint o1, AmphiroAbstractDataPoint o2) {
						if (o1.getTimestamp() <= o2.getTimestamp()) {
							return -1;
						} else {
							return 1;
						}
					}
				});
			}

			return data;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			try {
				if (scanner != null) {
					scanner.close();
				}
				if (table != null) {
					table.close();
				}
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	@Override
	public AmphiroSessionCollectionQueryResult searchSessions(String[] names, DateTimeZone timezone,
					AmphiroSessionCollectionQuery query) {
		AmphiroSessionCollectionQueryResult data = new AmphiroSessionCollectionQueryResult();

		DateTime startDate = null;
		if (query.getStartDate() != null) {
			startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
		} else {
			startDate = new DateTime(0L, DateTimeZone.UTC);
		}
		DateTime endDate = null;
		if (query.getEndDate() != null) {
			endDate = new DateTime(query.getEndDate(), DateTimeZone.UTC);
		} else {
			endDate = new DateTime(DateTimeZone.UTC);
		}

		switch (query.getGranularity()) {
			case TemporalConstants.NONE:
				// Retrieve values at the highest granularity, that is at the
				// measurement level
				break;
			case TemporalConstants.HOUR:
				startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(),
								startDate.getHourOfDay(), 0, 0, DateTimeZone.UTC);
				endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(),
								endDate.getHourOfDay(), 59, 59, DateTimeZone.UTC);
				break;
			case TemporalConstants.DAY:
				startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
								0, 0, DateTimeZone.UTC);
				endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23, 59,
								59, DateTimeZone.UTC);
				break;
			case TemporalConstants.WEEK:
				DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
				DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
				startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0, 0,
								DateTimeZone.UTC);
				endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59, 59,
								DateTimeZone.UTC);
				break;
			case TemporalConstants.MONTH:
				startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0, DateTimeZone.UTC);
				endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
								.getMaximumValue(), 23, 59, 59, DateTimeZone.UTC);
				break;
			case TemporalConstants.YEAR:
				startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
				endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, DateTimeZone.UTC);
				break;
			default:
				throw new ApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
								query.getGranularity());
		}

		boolean autoClose = false;

		Table table = null;
		ResultScanner scanner = null;

		try {

			if (!this.isOpen()) {
				this.open();
				autoClose = true;
			}

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.amphiroTableSessionByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			UUID deviceKeys[] = query.getDeviceKey();

			for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
				ArrayList<AmphiroSession> sessions = new ArrayList<AmphiroSession>();

				byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				Scan scan = new Scan();
				scan.addFamily(columnFamily);
				scan.setStartRow(this.getUserDeviceDayRowKey(userKeyHash, deviceKeyHash, startDate));
				scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getUserDeviceDayRowKey(userKeyHash,
								deviceKeyHash, endDate)));

				scanner = table.getScanner(scan);

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

					AmphiroSession session = new AmphiroSession();
					session.setId(Bytes.toLong(Arrays.copyOfRange(r.getRow(), 40, 48)));

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
								session.addProperty(qualifier, new String(entry.getValue(), StandardCharsets.UTF_8));
								break;
						}
					}

					if ((session.getTimestamp() >= startDate.getMillis())
									&& (session.getTimestamp() <= endDate.getMillis())) {
						sessions.add(session);
					}
				}

				scanner.close();
				scanner = null;

				AmphiroSessionCollection collection = new AmphiroSessionCollection(deviceKeys[deviceIndex],
								names[deviceIndex], query.getGranularity());

				collection.addSessions(sessions, timezone);

				if (collection.getSessions().size() > 0) {
					Collections.sort(collection.getSessions(), new Comparator<AmphiroAbstractSession>() {
						@Override
						public int compare(AmphiroAbstractSession o1, AmphiroAbstractSession o2) {
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

			return data;
		} catch (Exception ex) {
			autoClose = true;

			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			try {
				if (scanner != null) {
					scanner.close();
				}
				if (table != null) {
					table.close();
				}
				if (autoClose) {
					this.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	private byte[] calculateTheClosestNextRowKeyForPrefix(byte[] rowKeyPrefix) {
		// Essentially we are treating it like an 'unsigned very very long' and
		// doing +1 manually.
		// Search for the place where the trailing 0xFFs start
		int offset = rowKeyPrefix.length;
		while (offset > 0) {
			if (rowKeyPrefix[offset - 1] != (byte) 0xFF) {
				break;
			}
			offset--;
		}

		if (offset == 0) {
			// We got an 0xFFFF... (only FFs) stopRow value which is
			// the last possible prefix before the end of the table.
			// So set it to stop at the 'end of the table'
			return HConstants.EMPTY_END_ROW;
		}

		// Copy the right length of the original
		byte[] newStopRow = Arrays.copyOfRange(rowKeyPrefix, 0, offset);
		// And increment the last one
		newStopRow[newStopRow.length - 1]++;
		return newStopRow;
	}

	@Override
	public AmphiroSessionQueryResult getSession(AmphiroSessionQuery query) {
		AmphiroSessionQueryResult data = new AmphiroSessionQueryResult();

		// Compute temporal buffer
		DateTime startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
		DateTime endDate = new DateTime(query.getEndDate(), DateTimeZone.UTC);

		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		try {
			Configuration config = this.configurationBuilder.build();

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.amphiroTableSessionByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			byte[] deviceKey = query.getDeviceKey().toString().getBytes("UTF-8");
			byte[] deviceKeyHash = md.digest(deviceKey);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);

			scan.setStartRow(this.getUserDeviceDayRowKey(userKeyHash, deviceKeyHash, startDate));
			scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getUserDeviceDayRowKey(userKeyHash,
							deviceKeyHash, endDate)));

			scanner = table.getScanner(scan);

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

				AmphiroSessionDetails session = new AmphiroSessionDetails();
				session.setId(Bytes.toLong(Arrays.copyOfRange(r.getRow(), 40, 48)));

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
							session.addProperty(qualifier, new String(entry.getValue(), StandardCharsets.UTF_8));
							break;
					}
				}

				if ((session.getTimestamp() >= startDate.getMillis())
								&& (session.getTimestamp() <= endDate.getMillis())
								&& (session.getId() == query.getSessionId())) {

					session.setMeasurements(this.getSessionMeasurements(query, connection));

					data.setSession(session);
					break;
				}
			}

			return data;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			try {
				if (scanner != null) {
					scanner.close();
				}
				if (table != null) {
					table.close();
				}
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	private ArrayList<AmphiroMeasurement> getSessionMeasurements(AmphiroSessionQuery query, Connection connection) {
		ArrayList<AmphiroMeasurement> measurements = new ArrayList<AmphiroMeasurement>();

		DateTime startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
		DateTime endDate = (new DateTime(query.getEndDate(), DateTimeZone.UTC)).plusHours(12);

		Table table = null;
		ResultScanner scanner = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.amphiroTableMeasurements));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyKey = md.digest(userKey);

			byte[] deviceKey = query.getDeviceKey().toString().getBytes("UTF-8");
			byte[] deviceKeyHash = md.digest(deviceKey);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getUserDeviceHourRowKey(userKeyKey, deviceKeyHash, startDate));
			scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getUserDeviceHourRowKey(userKeyKey,
							deviceKeyHash, endDate)));

			scanner = table.getScanner(scan);

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

				short offset = -1;

				AmphiroMeasurement measurement = null;

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					short entryOffset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

					if (offset != entryOffset) {
						if ((measurement != null) && (measurement.getSessionId() == query.getSessionId())) {
							measurements.add(measurement);
						}
						offset = entryOffset;
						measurement = new AmphiroMeasurement();
						measurement.setTimestamp((timeBucket + offset) * 1000L);
					}

					int length = Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
					byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

					String qualifier = Bytes.toString(slice);

					switch (qualifier) {
						case "h":
							measurement.setHistory(Bytes.toBoolean(entry.getValue()));
							break;
						case "v":
							measurement.setVolume(Bytes.toFloat(entry.getValue()));
							break;
						case "e":
							measurement.setEnergy(Bytes.toFloat(entry.getValue()));
							break;
						case "t":
							measurement.setTemperature(Bytes.toFloat(entry.getValue()));
							break;
						case "s":
							measurement.setSessionId(Bytes.toLong(entry.getValue()));
							break;
						case "i":
							measurement.setIndex(Bytes.toInt(entry.getValue()));
							break;
					}
				}
				if ((measurement != null) && (measurement.getSessionId() == query.getSessionId())) {
					measurements.add(measurement);
				}
			}

			return measurements;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			try {
				if (scanner != null) {
					scanner.close();
				}
				if (table != null) {
					table.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	@Override
	public ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException {
		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		ArrayList<GroupDataSeries> result = new ArrayList<GroupDataSeries>();
		for (ExpandedPopulationFilter filter : query.getGroups()) {
			result.add(new GroupDataSeries(filter.getLabel(), filter.getUsers().size()));
		}
		try {
			Configuration config = this.configurationBuilder.build();
			connection = ConnectionFactory.createConnection(config);

			table = connection.getTable(TableName.valueOf(this.amphiroTableSessionByTime));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			DateTime startDate = new DateTime(query.getStartDateTime(), DateTimeZone.UTC);
			DateTime endDate = new DateTime(query.getEndDateTime(), DateTimeZone.UTC);

			switch (query.getGranularity()) {
				case HOUR:
					startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
									startDate.getDayOfMonth(), startDate.getHourOfDay(), 0, 0, DateTimeZone.UTC);
					endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(),
									endDate.getHourOfDay(), 59, 59, DateTimeZone.UTC);
					break;
				case DAY:
					startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
									startDate.getDayOfMonth(), 0, 0, 0, DateTimeZone.UTC);
					endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23,
									59, 59, DateTimeZone.UTC);
					break;
				case WEEK:
					DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
					DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
					startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0,
									0, DateTimeZone.UTC);
					endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59,
									59, DateTimeZone.UTC);
					break;
				case MONTH:
					startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0,
									DateTimeZone.UTC);
					endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
									.getMaximumValue(), 23, 59, 59, DateTimeZone.UTC);
					break;
				case YEAR:
					startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
					endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, DateTimeZone.UTC);
					break;
				case ALL:
					// Ignore
					break;
				default:
					throw new ApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
									query.getGranularity());
			}

			for (short p = 0; p < timePartitions; p++) {
				Scan scan = new Scan();
				scan.setCaching(this.scanCacheSize);
				scan.addFamily(columnFamily);

				byte[] partitionBytes = Bytes.toBytes(p);

				long from = startDate.getMillis() / 1000;
				from = from - (from % EnumTimeInterval.DAY.getValue());
				byte[] fromBytes = Bytes.toBytes(from);

				long to = endDate.getMillis() / 1000;
				to = to - (to % EnumTimeInterval.DAY.getValue());
				byte[] toBytes = Bytes.toBytes(to);

				// Scanner row key prefix start
				byte[] rowKey = new byte[partitionBytes.length + fromBytes.length];

				System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
				System.arraycopy(fromBytes, 0, rowKey, partitionBytes.length, fromBytes.length);

				scan.setStartRow(rowKey);

				// Scanner row key prefix end
				rowKey = new byte[partitionBytes.length + toBytes.length];

				System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
				System.arraycopy(toBytes, 0, rowKey, partitionBytes.length, toBytes.length);

				scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(rowKey));

				scanner = table.getScanner(scan);

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

					if (map != null) {
						rowKey = r.getRow();

						long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 2, 10));
						byte[] userHash = Arrays.copyOfRange(r.getRow(), 10, 26);

						int filterIndex = 0;
						for (ExpandedPopulationFilter filter : query.getGroups()) {
							GroupDataSeries series = result.get(filterIndex);

							if (inArray(filter.getHashes(), userHash)) {
								long timestamp = 0;
								float volume = 0;
								float energy = 0;
								int duration = 0;
								float temperature = 0;
								float flow = 0;

								for (Entry<byte[], byte[]> entry : map.entrySet()) {
									String qualifier = Bytes.toString(entry.getKey());

									switch (qualifier) {
										case "m:offset":
											timestamp = (timeBucket + Bytes.toInt(entry.getValue())) * 1000L;
											break;
										case "m:v":
											volume = Bytes.toFloat(entry.getValue());
											break;
										case "m:t":
											temperature = Bytes.toFloat(entry.getValue());
											break;
										case "m:e":
											energy = Bytes.toFloat(entry.getValue());
											break;
										case "m:f":
											flow = Bytes.toFloat(entry.getValue());
											break;
										case "m:d":
											duration = Bytes.toInt(entry.getValue());
											break;
										default:
											// Ignore
											break;
									}
								}

								series.addDataPoint(query.getGranularity(), timestamp, volume, energy, duration,
												temperature, flow, query.getMetrics(), query.getTimezone());
							}

							filterIndex++;
						}
					}
				}
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			try {
				if (scanner != null) {
					scanner.close();
				}
				if (table != null) {
					table.close();
				}
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}

		cleanSeries(result);

		return result;
	}

	private void cleanSeries(ArrayList<GroupDataSeries> result) {
		for (GroupDataSeries series : result) {
			for (Object p : series.getPoints()) {
				((AmphiroDataPoint) p).getTemperature().remove(EnumMetric.SUM);
				((AmphiroDataPoint) p).getFlow().remove(EnumMetric.SUM);
			}
		}
	}

	private boolean inArray(ArrayList<byte[]> group, byte[] hash) {
		for (byte[] entry : group) {
			if (Arrays.equals(entry, hash)) {
				return true;
			}
		}
		return false;
	}
}
