package eu.daiad.web.data.hbase;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.DataPoint;
import eu.daiad.web.model.DataSeries;
import eu.daiad.web.model.DayIntervalDataPointCollection;
import eu.daiad.web.model.HourlyDataPoints;
import eu.daiad.web.model.Measurement;
import eu.daiad.web.model.MeasurementCollection;
import eu.daiad.web.model.MeasurementQuery;
import eu.daiad.web.model.MeasurementResult;
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
import eu.daiad.web.model.SmartMeterMeasurementCollection;
import eu.daiad.web.model.SmartMeterQuery;
import eu.daiad.web.model.SmartMeterResult;
import eu.daiad.web.model.TemporalConstants;

@Service()
@Scope("prototype")
public class HbaseConnection {

	private String quorum;

	private String amphiroTableName;

	private String smartMeterTableName;

	private String columnFamilyName;

	public enum ValueType {
		Short(1), Integer(2), Long(3), Float(4), Double(5), String(6);

		private int value;

		private ValueType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	private static final Log logger = LogFactory.getLog(HbaseConnection.class);

	@Autowired
	public HbaseConnection(
			@Value("${hbase.zookeeper.quorum}") String quorum,
			@Value("${hbase.data.amphiro.table}") String amphiroTableName,
			@Value("${hbase.data.swm.table}") String smartMeterTableName,
			@Value("${hbase.data.amphiro.column-family}") String columnFamilyName) {
		this.quorum = quorum;
		this.amphiroTableName = amphiroTableName;
		this.smartMeterTableName = smartMeterTableName;
		this.columnFamilyName = columnFamilyName;
	}

	public void storeDataAmphiro(MeasurementCollection data) {
		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.amphiroTableName));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			for (int i = 0; i < data.getMeasurements().size(); i++) {
				Measurement m = data.getMeasurements().get(i);

				if (m.volume <= 0) {
					continue;
				}

				byte[] applicationKey = data.getApplicationKey().toString()
						.getBytes("UTF-8");
				byte[] applicationKeyHash = md.digest(applicationKey);

				byte[] deviceId = data.getDeviceId().toString()
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
			table.close();
			connection.close();
		} catch (RuntimeException ex) {
			logger.error("Malformed data found.");
			logger.error(ex);
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.");
			logger.error(ex);
		}
	}

	public void storeDataSmartMeter(SmartMeterMeasurementCollection data) {
		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			Connection connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			Table table = connection.getTable(TableName
					.valueOf(this.smartMeterTableName));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] applicationKey = data.getApplicationKey().toString()
					.getBytes("UTF-8");
			byte[] applicationKeyHash = md.digest(applicationKey);

			byte[] deviceId = data.getDeviceId().toString().getBytes("UTF-8");
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
			logger.error("Malformed data found.");
			logger.error(ex);
		} catch (Exception ex) {
			logger.error("Unhandled exception has occured.");
			logger.error(ex);
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
			logger.error("Unhandled exception has occured.");
			logger.error(ex);
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
			logger.error("Unhandled exception has occured.");
			logger.error(ex);
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
			logger.error("Unhandled exception has occured.");
			logger.error(ex);
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

		DateTime now = new DateTime();
		long timestampNow = now.plusDays(1).getMillis();
		if (timestampNow < endDate.getMillis()) {
			endDate = now.plusDays(1);
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
			//scan.setStopRow(this.getRowKey(applicationKeyHash, deviceIdHash, endDate));

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
								&& (point.timestamp <= timestampNow)) {
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
				if ((point != null) && (point.timestamp <= timestampNow)) {
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
			logger.error("Unhandled exception has occured.");
			logger.error(ex);
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
			logger.error("Unhandled exception has occured.");
			logger.error(ex);
		}

		return null;
	}

}