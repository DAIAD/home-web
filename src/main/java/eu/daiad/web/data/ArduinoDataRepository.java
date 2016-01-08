package eu.daiad.web.data;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.lang.StringUtils;
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

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.arduino.ArduinoIntervalQuery;
import eu.daiad.web.model.arduino.ArduinoIntervalQueryResult;
import eu.daiad.web.model.arduino.ArduinoMeasurement;

@Repository()
@Scope("prototype")
@PropertySource("${hbase.properties}")
public class ArduinoDataRepository {

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

	private String arduinoTableMeasurements = "daiad:arduino-measurements";

	private String columnFamilyName = "cf";

	private Connection connection = null;

	private Table table = null;

	@Value("${arduino.device.default}")
	private String defaultArduinoDeviceKey;

	private static final Log logger = LogFactory
			.getLog(ArduinoDataRepository.class);

	@Autowired
	public ArduinoDataRepository(
			@Value("${hbase.zookeeper.quorum}") String quorum) {
		this.quorum = quorum;
	}

	public void storeData(String deviceKey, ArrayList<ArduinoMeasurement> data)
			throws Exception {

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName
					.valueOf(this.arduinoTableMeasurements));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			if (StringUtils.isBlank(deviceKey)) {
				deviceKey = defaultArduinoDeviceKey;
			}

			byte[] deviceKeyBytes = deviceKey.getBytes("UTF-8");
			byte[] deviceKeyHash = md.digest(deviceKeyBytes);

			for (int i = 0; i < data.size(); i++) {
				ArduinoMeasurement m = data.get(i);

				if (m.getVolume() <= 0) {
					continue;
				}

				long timestamp = (Long.MAX_VALUE - (m.getTimestamp() / 1000));

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

				byte[] rowKey = new byte[deviceKeyHash.length
						+ timeBucketBytes.length];
				System.arraycopy(deviceKeyHash, 0, rowKey, 0,
						deviceKeyHash.length);
				System.arraycopy(timeBucketBytes, 0, rowKey,
						(deviceKeyHash.length), timeBucketBytes.length);

				Put p = new Put(rowKey);

				byte[] column = this.concatenate(timeSliceBytes,
						this.appendLength(Bytes.toBytes("v")));
				p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

				table.put(p);
			}
		} finally {
			try {
				if (table != null) {
					table.close();
					table = null;
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (IOException ioEx) {
				logger.error(
						"Failed to release HBASE connection/table resources",
						ioEx);
			}
		}
	}

	private byte[] getDeviceTimeRowKey(byte[] deviceKeyHash, long date,
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
			throw new RuntimeException(
					String.format("Time interval [%s] is not supported.",
							interval.toString()));
		}

		long timestamp = Long.MAX_VALUE - (date / 1000);
		long timeSlice = timestamp % intervalInSeconds;
		long timeBucket = timestamp - timeSlice;
		byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

		byte[] rowKey = new byte[deviceKeyHash.length + 8];
		System.arraycopy(deviceKeyHash, 0, rowKey, 0, deviceKeyHash.length);
		System.arraycopy(timeBucketBytes, 0, rowKey, deviceKeyHash.length,
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

	public ArduinoIntervalQueryResult searchData(ArduinoIntervalQuery query)
			throws Exception {
		ArduinoIntervalQueryResult data = new ArduinoIntervalQueryResult();

		Connection connection = null;
		Table table = null;

		try {
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName
					.valueOf(this.arduinoTableMeasurements));
			byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

			byte[] deviceKeyBytes = query.getDeviceKey().getBytes("UTF-8");
			byte[] deviceKeyHash = md.digest(deviceKeyBytes);

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			scan.setStartRow(this.getDeviceTimeRowKey(deviceKeyHash,
					query.getTimestampEnd(), EnumTimeInterval.HOUR));

			ResultScanner scanner = table.getScanner(scan);

			boolean isScanCompleted = false;

			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(),
						16, 24));

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					short offset = Bytes.toShort(Arrays.copyOfRange(
							entry.getKey(), 0, 2));

					long timestamp = (Long.MAX_VALUE - (timeBucket + (long) offset)) * 1000L;

					int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
					byte[] slice = Arrays.copyOfRange(entry.getKey(), 3,
							3 + length);

					String columnQualifier = Bytes.toString(slice);
					if (columnQualifier.equals("v")) {
						if (timestamp < query.getTimestampStart()) {
							isScanCompleted = true;
							break;
						}

						if (timestamp <= query.getTimestampEnd()) {
							long volume = Bytes.toLong(entry.getValue());

							data.add(timestamp, volume);
						}
					}
				}
				if (isScanCompleted) {
					break;
				}
			}
			scanner.close();

			Collections.sort(data.getMeasurements(),
					new Comparator<ArduinoMeasurement>() {

						public int compare(ArduinoMeasurement o1, ArduinoMeasurement o2) {
							if (o1.getTimestamp() <= o2.getTimestamp()) {
								return -1;
							} else {
								return 1;
							}
						}
					});
		} finally {
			try {
				if (table != null) {
					table.close();
					table = null;
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (IOException ioEx) {
				logger.error(
						"Failed to release HBASE connection/table resources",
						ioEx);
			}
		}

		return data;
	}

}