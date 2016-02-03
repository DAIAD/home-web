package eu.daiad.web.data;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;

@Repository()
@Scope("prototype")
@PropertySource("${hbase.properties}")
public class HBaseWaterMeterMeasurementRepository implements IWaterMeterMeasurementRepository {

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

	private String quorum;

	private String meterTableMeasurementByUser = "daiad:meter-measurements-by-time";
	private String meterTableMeasurementByTime = "daiad:meter-measurements-by-user";

	private String columnFamilyName = "cf";

	@Value("${hbase.data.time.partitions}")
	private short timePartitions;

	private static final Log logger = LogFactory.getLog(HBaseWaterMeterMeasurementRepository.class);

	@Autowired
	public HBaseWaterMeterMeasurementRepository(@Value("${hbase.zookeeper.quorum}") String quorum) {
		this.quorum = quorum;
	}

	@Override
	public void storeData(WaterMeterMeasurementCollection data) {
		Connection connection = null;

		try {
			if ((data == null) || (data.getMeasurements() == null) || (data.getMeasurements().size() == 0)) {
				return;
			}
			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			this.storeDataByUser(connection, data);
			this.storeDataByTime(connection, data);
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		} finally {
			try {
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	@SuppressWarnings("resource")
	private void storeDataByUser(Connection connection, WaterMeterMeasurementCollection data) {
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = data.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
			byte[] deviceKeyHash = md.digest(deviceKey);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				WaterMeterMeasurement m = data.getMeasurements().get(i);

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

				byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + timeBucketBytes.length];
				System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length, deviceKeyHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey, (userKeyHash.length + deviceKeyHash.length),
								timeBucketBytes.length);

				Put p = new Put(rowKey);

				byte[] column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

				table.put(p);
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
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

	@SuppressWarnings("resource")
	private void storeDataByTime(Connection connection, WaterMeterMeasurementCollection data) {
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByTime));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = data.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			byte[] deviceKey = data.getDeviceKey().toString().getBytes("UTF-8");
			byte[] deviceKeyHash = md.digest(deviceKey);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				WaterMeterMeasurement m = data.getMeasurements().get(i);

				if (m.getVolume() <= 0) {
					continue;
				}
				short partition = (short) (m.getTimestamp() % this.timePartitions);
				byte[] partitionBytes = Bytes.toBytes(partition);

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

				byte[] rowKey = new byte[partitionBytes.length + timeBucketBytes.length + userKeyHash.length
								+ deviceKeyHash.length];

				System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
				System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);
				System.arraycopy(userKeyHash, 0, rowKey, (partitionBytes.length + timeBucketBytes.length),
								userKeyHash.length);
				System.arraycopy(deviceKeyHash, 0, rowKey,
								(partitionBytes.length + timeBucketBytes.length + userKeyHash.length),
								deviceKeyHash.length);

				Put p = new Put(rowKey);

				byte[] column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

				table.put(p);
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
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

		byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length + 8];
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
	public WaterMeterStatusQueryResult getStatus(WaterMeterStatusQuery query) {
		WaterMeterStatusQueryResult data = new WaterMeterStatusQueryResult();
		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			UUID deviceKeys[] = query.getDeviceKey();
			for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
				byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				DateTime maxDate = new DateTime();

				Scan scan = new Scan();
				scan.addFamily(columnFamily);
				scan.setStartRow(this.getUserDeviceTimeRowKey(userKeyHash, deviceKeyHash,
								(Long.MAX_VALUE - maxDate.getMillis()), EnumTimeInterval.HOUR));
				scan.setCaching(2);

				scanner = table.getScanner(scan);

				long lastTimeBucket = 0;
				int bucketCount = 0;
				int valueCount = 0;

				WaterMeterStatus status = new WaterMeterStatus();
				WaterMeterDataPoint value1 = new WaterMeterDataPoint();
				WaterMeterDataPoint value2 = new WaterMeterDataPoint();

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					if (bucketCount > 2) {
						break;
					}

					NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));
					if (lastTimeBucket != timeBucket) {
						bucketCount++;
					}
					lastTimeBucket = timeBucket;

					for (Entry<byte[], byte[]> entry : map.entrySet()) {
						short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

						long timestamp = Long.MAX_VALUE - ((timeBucket + (long) offset) * 1000L);

						int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
						byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);
						String columnQualifier = Bytes.toString(slice);
						if (columnQualifier.equals("v")) {
							valueCount++;
							if (value2.timestamp < timestamp) {
								value1.timestamp = value2.timestamp;
								value1.volume = value2.volume;

								value2.timestamp = timestamp;
								value2.volume = Bytes.toFloat(entry.getValue());
							} else if (value1.timestamp < timestamp) {
								value1.timestamp = timestamp;
								value1.volume = Bytes.toFloat(entry.getValue());
							}
						}
					}
				}

				switch (valueCount) {
				case 0:
					// No value found
					break;
				case 1:
					status.setTimestamp(value2.timestamp);
					status.setVolume(value2.volume);
					status.setVariation(0);
					status.setDeviceKey(deviceKeys[deviceIndex]);

					data.getDevices().add(status);
				default:
					status.setTimestamp(value2.timestamp);
					status.setVolume(value2.volume);
					status.setVariation(value2.volume - value1.volume);
					status.setDeviceKey(deviceKeys[deviceIndex]);

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
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error(ERROR_RELEASE_RESOURCES, ex);
			}
		}
	}

	@Override
	public WaterMeterMeasurementQueryResult searchMeasurements(WaterMeterMeasurementQuery query) {
		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		DateTime startDate = new DateTime(query.getStartDate());
		DateTime endDate = new DateTime(query.getEndDate());

		switch (query.getGranularity()) {
		case TemporalConstants.NONE:
			// Retrieve values at the highest granularity, that is at the
			// measurement level
			break;
		case TemporalConstants.HOUR:
			startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(),
							startDate.getHourOfDay(), 0, 0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(),
							endDate.getHourOfDay(), 59, 59);
			break;
		case TemporalConstants.DAY:
			startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0, 0,
							0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23, 59, 59);
			break;
		case TemporalConstants.WEEK:
			DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
			DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
			startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0, 0);
			endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59, 59);
			break;
		case TemporalConstants.MONTH:
			startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0);
			endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth().getMaximumValue(),
							23, 59, 59);
			break;
		case TemporalConstants.YEAR:
			startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0);
			endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59);
			break;
		default:
			throw new ApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
							query.getGranularity());
		}

		DateTime queryStartDate = startDate;
		DateTime queryEndDate = endDate;

		DateTime maxDate = new DateTime();
		if (maxDate.getMillis() < endDate.getMillis()) {
			endDate = maxDate;
		}

		WaterMeterMeasurementQueryResult data = new WaterMeterMeasurementQueryResult();

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.meterTableMeasurementByUser));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] userKey = query.getUserKey().toString().getBytes("UTF-8");
			byte[] userKeyHash = md.digest(userKey);

			UUID deviceKeys[] = query.getDeviceKey();
			for (int deviceIndex = 0; deviceIndex < deviceKeys.length; deviceIndex++) {
				byte[] deviceKey = deviceKeys[deviceIndex].toString().getBytes("UTF-8");
				byte[] deviceKeyHash = md.digest(deviceKey);

				Scan scan = new Scan();
				scan.addFamily(columnFamily);
				scan.setStartRow(this.getUserDeviceTimeRowKey(userKeyHash, deviceKeyHash,
								(Long.MAX_VALUE - endDate.getMillis()), EnumTimeInterval.HOUR));

				scanner = table.getScanner(scan);

				boolean stopScanner = false;

				WaterMeterDataSeries series = new WaterMeterDataSeries(queryStartDate.getMillis(),
								queryEndDate.getMillis(), query.getGranularity());

				series.setDeviceKey(deviceKeys[deviceIndex]);
				data.getSeries().add(series);

				for (Result r = scanner.next(); r != null; r = scanner.next()) {
					NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

					long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 32, 40));

					for (Entry<byte[], byte[]> entry : map.entrySet()) {
						short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

						long timestamp = Long.MAX_VALUE - ((timeBucket + (long) offset) * 1000L);

						int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
						byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

						String columnQualifier = Bytes.toString(slice);
						if (columnQualifier.equals("v")) {
							float volume = Bytes.toFloat(entry.getValue());
							series.add(timestamp, volume);
							if (queryStartDate.getMillis() > timestamp) {
								stopScanner = true;
								break;
							}
						}
					}
					if (stopScanner) {
						break;
					}
				}

				Collections.sort(series.getValues(), new Comparator<WaterMeterDataPoint>() {
					public int compare(WaterMeterDataPoint o1, WaterMeterDataPoint o2) {
						if (o1.timestamp <= o2.timestamp) {
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

}
