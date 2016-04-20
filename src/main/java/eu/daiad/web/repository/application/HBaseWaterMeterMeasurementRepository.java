package eu.daiad.web.repository.application;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
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
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DataErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.meter.WaterMeterDataPoint;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurement;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.GroupDataSeries;

@Repository()
@Scope("prototype")
public class HBaseWaterMeterMeasurementRepository implements IWaterMeterMeasurementRepository {

	private static final Log logger = LogFactory.getLog(HBaseWaterMeterMeasurementRepository.class);

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

	private String meterTableMeasurementByMeter = "daiad:meter-measurements-by-user";

	private String meterTableMeasurementByTime = "daiad:meter-measurements-by-time";

	private String columnFamilyName = "cf";

	@Value("${hbase.data.time.partitions}")
	private short timePartitions;

	@Value("${scanner.cache.size}")
	private int scanCacheSize = 1;

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

	@Override
	public void storeData(String serial, WaterMeterMeasurementCollection data) {
		boolean autoClose = false;

		try {
			if ((data == null) || (data.getMeasurements() == null) || (data.getMeasurements().size() == 0)) {
				return;
			}
			if (!this.isOpen()) {
				this.open();
				autoClose = true;
			}

			// Sort measurements
			Collections.sort(data.getMeasurements(), new Comparator<WaterMeterMeasurement>() {
				public int compare(WaterMeterMeasurement o1, WaterMeterMeasurement o2) {
					if (o1.getTimestamp() <= o2.getTimestamp()) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			// Get current status if no difference is computed
			WaterMeterMeasurement first = data.getMeasurements().get(0);
			if (first.getDifference() == null) {
				WaterMeterStatusQueryResult status = this.getStatus(new String[] { serial },
								new DateTime(first.getTimestamp(), DateTimeZone.UTC).getMillis());

				if (status.getDevices().size() == 0) {
					// This is the first measurement for this water meter
					first.setDifference(0.0f);
				} else if (first.getTimestamp() == status.getDevices().get(0).getTimestamp()) {
					first.setDifference(first.getVolume() - status.getDevices().get(0).getVolume()
									+ status.getDevices().get(0).getVariation());
				} else {
					first.setDifference(first.getVolume() - status.getDevices().get(0).getVolume());
				}
			}
			for (int i = 1, count = data.getMeasurements().size(); i < count; i++) {
				data.getMeasurements()
								.get(i)
								.setDifference(data.getMeasurements().get(i).getVolume()
												- data.getMeasurements().get(i - 1).getVolume());
			}

			this.storeDataByMeter(connection, serial, data);
			this.storeDataByTime(connection, serial, data);
		} catch (Exception ex) {
			autoClose = true;

			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			if (autoClose) {
				this.close();
			}
		}
	}

	@SuppressWarnings("resource")
	private void storeDataByMeter(Connection connection, String serial, WaterMeterMeasurementCollection data) {
		Table table = null;
		try {
			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByMeter));

			MessageDigest md = MessageDigest.getInstance("MD5");

			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] meterSerial = serial.getBytes("UTF-8");
			byte[] meterSerialHash = md.digest(meterSerial);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				WaterMeterMeasurement m = data.getMeasurements().get(i);

				if (m.getVolume() <= 0) {
					continue;
				}

				long timestamp = (Long.MAX_VALUE / 1000) - (m.getTimestamp() / 1000);

				long timeSlice = timestamp % EnumTimeInterval.HOUR.getValue();
				byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);
				if (timeSliceBytes.length != 2) {
					throw new RuntimeException("Invalid byte array length!");
				}

				long timeBucket = timestamp - timeSlice;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] rowKey = new byte[meterSerialHash.length + timeBucketBytes.length];
				System.arraycopy(meterSerialHash, 0, rowKey, 0, meterSerialHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey, meterSerialHash.length, timeBucketBytes.length);

				Put p = new Put(rowKey);

				byte[] column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

				column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("d")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

				table.put(p);
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			try {
				if (table != null) {
					table.close();
					table = null;
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	@SuppressWarnings("resource")
	private void storeDataByTime(Connection connection, String serial, WaterMeterMeasurementCollection data) {
		Table table = null;

		try {
			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByTime));

			MessageDigest md = MessageDigest.getInstance("MD5");

			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] meterSerial = serial.getBytes("UTF-8");
			byte[] meterSerialHash = md.digest(meterSerial);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				WaterMeterMeasurement m = data.getMeasurements().get(i);

				if (m.getVolume() <= 0) {
					continue;
				}
				short partition = (short) (m.getTimestamp() % this.timePartitions);
				byte[] partitionBytes = Bytes.toBytes(partition);

				long timestamp = (Long.MAX_VALUE / 1000) - (m.getTimestamp() / 1000);

				long timeSlice = timestamp % EnumTimeInterval.HOUR.getValue();
				byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);
				if (timeSliceBytes.length != 2) {
					throw new RuntimeException("Invalid byte array length!");
				}

				long timeBucket = timestamp - timeSlice;

				byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
				if (timeBucketBytes.length != 8) {
					throw new RuntimeException("Invalid byte array length!");
				}

				byte[] rowKey = new byte[partitionBytes.length + timeBucketBytes.length + meterSerialHash.length];

				System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
				System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);
				System.arraycopy(meterSerialHash, 0, rowKey, (partitionBytes.length + timeBucketBytes.length),
								meterSerialHash.length);

				Put p = new Put(rowKey);

				byte[] column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

				column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("d")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

				table.put(p);
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			try {
				if (table != null) {
					table.close();
					table = null;
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	private byte[] getDeviceTimeRowKey(byte[] meterSerialHash, long timestamp, EnumTimeInterval interval)
					throws Exception {

		long intervalInSeconds = EnumTimeInterval.HOUR.getValue();
		switch (interval) {
			case HOUR:
				intervalInSeconds = interval.getValue();
				break;

			default:
				throw new RuntimeException(String.format("Time interval [%s] is not supported.", interval.toString()));
		}

		long timeSlice = timestamp % intervalInSeconds;
		long timeBucket = timestamp - timeSlice;
		byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

		byte[] rowKey = new byte[meterSerialHash.length + 8];
		System.arraycopy(meterSerialHash, 0, rowKey, 0, meterSerialHash.length);
		System.arraycopy(timeBucketBytes, 0, rowKey, meterSerialHash.length, timeBucketBytes.length);

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
	public WaterMeterStatusQueryResult getStatus(String serials[]) {
		return this.getStatus(serials, new DateTime(DateTimeZone.UTC).getMillis());
	}

	@Override
	public WaterMeterStatusQueryResult getStatus(String serials[], long maxDateTime) {
		boolean autoClose = false;

		WaterMeterStatusQueryResult data = new WaterMeterStatusQueryResult();

		Table table = null;
		ResultScanner scanner = null;

		try {
			if (!this.isOpen()) {
				this.open();
				autoClose = true;
			}

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByMeter));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int deviceIndex = 0; deviceIndex < serials.length; deviceIndex++) {
				byte[] meterSerial = serials[deviceIndex].getBytes("UTF-8");
				byte[] meterSerialHash = md.digest(meterSerial);

				Scan scan = new Scan();
				scan.addFamily(columnFamily);
				scan.setStartRow(this.getDeviceTimeRowKey(meterSerialHash, (Long.MAX_VALUE / 1000)
								- (maxDateTime / 1000), EnumTimeInterval.HOUR));
				scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(meterSerialHash));
				scan.setCaching(2);

				scanner = table.getScanner(scan);

				int valueCount = 0;

				WaterMeterStatus status = new WaterMeterStatus(serials[deviceIndex]);
				WaterMeterDataPoint value1 = new WaterMeterDataPoint();
				WaterMeterDataPoint value2 = new WaterMeterDataPoint();

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					if (valueCount == 2) {
						break;
					}

					NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

					for (Entry<byte[], byte[]> entry : map.entrySet()) {
						if (valueCount == 2) {
							break;
						}

						short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

						long timestamp = ((Long.MAX_VALUE / 1000) - (timeBucket + (long) offset)) * 1000L;

						if (timestamp <= maxDateTime) {
							int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
							byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);
							String columnQualifier = Bytes.toString(slice);

							if (columnQualifier.equals("v")) {
								valueCount++;
								if (value2.getTimestamp() < timestamp) {
									value1.setTimestamp(value2.getTimestamp());
									value1.setVolume(value2.getVolume());

									value2.setTimestamp(timestamp);
									value2.setVolume(Bytes.toFloat(entry.getValue()));
								} else if (value1.getTimestamp() < timestamp) {
									value1.setTimestamp(timestamp);
									value1.setVolume(Bytes.toFloat(entry.getValue()));
								}
							}
						}
					}
				}

				switch (valueCount) {
					case 0:
						// No value found
						break;
					case 1:
						status.setTimestamp(value2.getTimestamp());
						status.setVolume(value2.getVolume());
						status.setVariation(0);

						data.getDevices().add(status);
					default:
						status.setTimestamp(value2.getTimestamp());
						status.setVolume(value2.getVolume());
						status.setVariation(value2.getVolume() - value1.getVolume());

						data.getDevices().add(status);
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
				if (autoClose) {
					this.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	@Override
	public WaterMeterMeasurementQueryResult searchMeasurements(String serials[], DateTimeZone timezone,
					WaterMeterMeasurementQuery query) {
		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

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

		DateTime queryEndDate = endDate;

		DateTime maxDate = new DateTime(DateTimeZone.UTC);
		if (maxDate.getMillis() < endDate.getMillis()) {
			endDate = maxDate;
		}

		WaterMeterMeasurementQueryResult data = new WaterMeterMeasurementQueryResult();

		try {
			Configuration config = this.configurationBuilder.build();

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByMeter));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int deviceIndex = 0; deviceIndex < serials.length; deviceIndex++) {
				byte[] meterSerial = serials[deviceIndex].getBytes("UTF-8");
				byte[] meterSerialHash = md.digest(meterSerial);

				Scan scan = new Scan();
				scan.addFamily(columnFamily);

				scan.setStartRow(this.getDeviceTimeRowKey(meterSerialHash,
								(Long.MAX_VALUE / 1000L) - (endDate.getMillis() / 1000L), EnumTimeInterval.HOUR));

				scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getDeviceTimeRowKey(meterSerialHash,
								(Long.MAX_VALUE / 1000L) - (startDate.getMillis() / 1000L), EnumTimeInterval.HOUR)));

				scanner = table.getScanner(scan);

				WaterMeterDataSeries series = new WaterMeterDataSeries(query.getDeviceKey()[deviceIndex],
								serials[deviceIndex], startDate.getMillis(), queryEndDate.getMillis(),
								query.getGranularity());

				data.getSeries().add(series);

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

					float volume = -1, difference = -1;
					long timestamp = 0;

					for (Entry<byte[], byte[]> entry : map.entrySet()) {
						short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));
						timestamp = ((Long.MAX_VALUE / 1000L) - (timeBucket + (long) offset)) * 1000L;

						if ((startDate.getMillis() <= timestamp) && (timestamp <= endDate.getMillis())) {
							int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
							byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

							String columnQualifier = Bytes.toString(slice);
							if (columnQualifier.equals("v")) {
								volume = Bytes.toFloat(entry.getValue());
							}
							if (columnQualifier.equals("d")) {
								difference = Bytes.toFloat(entry.getValue());
							}

							if ((volume > 0) && (difference >= 0)) {
								series.add(timestamp, volume, difference, timezone);
								volume = -1;
								difference = -1;
							}
						}
					}

					if ((volume > 0) && (difference >= 0)) {
						series.add(timestamp, volume, difference, timezone);
						volume = 0;
						difference = 0;
					}

				}

				series.sort();
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

			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByTime));
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

				long from = (Long.MAX_VALUE / 1000) - (endDate.getMillis() / 1000);
				from = from - (from % EnumTimeInterval.HOUR.getValue());
				byte[] fromBytes = Bytes.toBytes(from);

				long to = (Long.MAX_VALUE / 1000) - (startDate.getMillis() / 1000);
				to = to - (to % EnumTimeInterval.HOUR.getValue());
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

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 2, 10));
					byte[] serialHash = Arrays.copyOfRange(r.getRow(), 10, 26);

					for (Entry<byte[], byte[]> entry : map.entrySet()) {
						short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

						long timestamp = ((Long.MAX_VALUE / 1000) - (timeBucket + (long) offset)) * 1000L;

						if ((startDate.getMillis() <= timestamp) && (timestamp <= endDate.getMillis())) {
							int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
							byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

							String columnQualifier = Bytes.toString(slice);
							if (columnQualifier.equals("d")) {
								float difference = Bytes.toFloat(entry.getValue());

								if (difference > 0) {
									int filterIndex = 0;
									for (ExpandedPopulationFilter filter : query.getGroups()) {
										GroupDataSeries series = result.get(filterIndex);

										if (inArray(filter.getSerials(), serialHash)) {
											series.addDataPoint(query.getGranularity(), timestamp, difference,
															query.getMetrics(), query.getTimezone());
										}

										filterIndex++;

									}
								}
							}
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

		return result;
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
