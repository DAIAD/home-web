package eu.daiad.web.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.UUID;

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
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;

@Repository()
@Scope("prototype")
@PropertySource("${hbase.properties}")
public class HBaseDeviceRepository implements IDeviceRepository {

	private String quorum;

	private final String deviceTable = "daiad:device";

	private final String columnFamily = "cf";

	@Autowired
	public HBaseDeviceRepository(
			@Value("${hbase.zookeeper.quorum}") String quorum) {
		this.quorum = quorum;
	}

	@Override
	public UUID createAmphiroDevice(UUID userKey, String name,
			String macAddress, ArrayList<KeyValuePair> properties)
			throws Exception {
		UUID deviceKey = UUID.randomUUID();
		Connection connection = null;
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			if (this.getUserAmphiroDeviceByMacAddress(userKey, macAddress) != null) {
				throw new Exception("Device already exists.");
			}
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			byte[] deviceKeyHash = md.digest(deviceKey.toString().getBytes(
					StandardCharsets.UTF_8));

			byte[] userKeyHash = md.digest(userKey.toString().getBytes(
					StandardCharsets.UTF_8));

			table = connection.getTable(TableName.valueOf(this.deviceTable));
			byte[] columnFamily = Bytes.toBytes(this.columnFamily);

			byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length];
			System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
			System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length,
					deviceKeyHash.length);

			Put p = new Put(rowKey);

			byte[] column = Bytes.toBytes("type");
			p.addColumn(columnFamily, column,
					Bytes.toBytes(EnumDeviceType.AMPHIRO.getValue()));
			column = Bytes.toBytes("key");
			p.addColumn(columnFamily, column,
					deviceKey.toString().getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("name");
			p.addColumn(columnFamily, column,
					name.getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("macAddress");
			p.addColumn(columnFamily, column,
					macAddress.getBytes(StandardCharsets.UTF_8));

			if (properties != null) {
				for (int i = 0, count = properties.size(); i < count; i++) {
					column = Bytes.toBytes(properties.get(i).getKey());
					p.addColumn(columnFamily, column, properties.get(i)
							.getValue().getBytes(StandardCharsets.UTF_8));
				}
			}

			table.put(p);
		} finally {
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}

		return deviceKey;
	}

	@Override
	public UUID createMeterDevice(UUID userKey, String serial,
			ArrayList<KeyValuePair> properties) throws Exception {
		UUID devicekey = UUID.randomUUID();
		Connection connection = null;
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			if (this.getUserWaterMeterDeviceBySerial(userKey, serial) != null) {
				throw new Exception("Device already exists.");
			}
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			byte[] deviceKeyHash = md.digest(devicekey.toString().getBytes(
					StandardCharsets.UTF_8));

			byte[] userKeyHash = md.digest(userKey.toString().getBytes(
					StandardCharsets.UTF_8));

			table = connection.getTable(TableName.valueOf(this.deviceTable));
			byte[] columnFamily = Bytes.toBytes(this.columnFamily);

			byte[] rowKey = new byte[userKeyHash.length + deviceKeyHash.length];
			System.arraycopy(userKeyHash, 0, rowKey, 0, userKeyHash.length);
			System.arraycopy(deviceKeyHash, 0, rowKey, userKeyHash.length,
					deviceKeyHash.length);

			Put p = new Put(rowKey);

			byte[] column = Bytes.toBytes("type");
			p.addColumn(columnFamily, column,
					Bytes.toBytes(EnumDeviceType.METER.getValue()));
			column = Bytes.toBytes("key");
			p.addColumn(columnFamily, column,
					devicekey.toString().getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("serial");
			p.addColumn(columnFamily, column,
					serial.getBytes(StandardCharsets.UTF_8));

			if (properties != null) {
				for (int i = 0, count = properties.size(); i < count; i++) {
					column = Bytes.toBytes(properties.get(i).getKey());
					p.addColumn(columnFamily, column, properties.get(i)
							.getValue().getBytes(StandardCharsets.UTF_8));
				}
			}

			table.put(p);
		} finally {
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}

		return devicekey;
	}

	@Override
	public Device getUserDeviceByKey(UUID userKey, UUID deviceKey)
			throws Exception {
		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);
			table = connection.getTable(TableName.valueOf(this.deviceTable));

			byte[] columnFamily = Bytes.toBytes(this.columnFamily);
			byte[] rowKey = md.digest(userKey.toString().getBytes("UTF-8"));

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			Filter prefixFilter = new PrefixFilter(rowKey);
			scan.setFilter(prefixFilter);

			UUID key = null;
			String name = null;
			String serial = null;
			String macAddress = null;
			EnumDeviceType type = EnumDeviceType.UNDEFINED;

			ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();

			scanner = table.getScanner(scan);
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				key = null;
				name = null;
				serial = null;
				macAddress = null;
				type = EnumDeviceType.UNDEFINED;

				properties.clear();

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					String qualifier = Bytes.toString(entry.getKey());

					switch (qualifier) {
					case "type":
						type = EnumDeviceType.fromInteger(Bytes.toInt(entry
								.getValue()));
						break;
					case "key":
						key = UUID.fromString(new String(entry.getValue(),
								StandardCharsets.UTF_8));
						break;
					case "name":
						name = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "serial":
						serial = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "macAddress":
						macAddress = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					default:
						properties.add(new KeyValuePair(qualifier, new String(
								entry.getValue(), StandardCharsets.UTF_8)));
					}
				}

				if ((key != null) && (key.equals(deviceKey))) {
					break;
				}
			}

			if ((key != null) && (key.equals(deviceKey))) {
				switch (type) {
				case METER:
					return new WaterMeterDevice(key, serial, properties);
				case AMPHIRO:
					return new AmphiroDevice(key, name, macAddress, properties);
				case UNDEFINED:
					break;
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

		return null;
	}

	@Override
	public Device getUserAmphiroDeviceByMacAddress(UUID userKey,
			String macAddress) throws Exception {
		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		AmphiroDevice device = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);
			table = connection.getTable(TableName.valueOf(this.deviceTable));

			byte[] columnFamily = Bytes.toBytes(this.columnFamily);
			byte[] rowKey = md.digest(userKey.toString().getBytes("UTF-8"));

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			Filter prefixFilter = new PrefixFilter(rowKey);
			scan.setFilter(prefixFilter);

			UUID key = null;
			String name = null;
			String deviceMacAddress = null;

			ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();

			scanner = table.getScanner(scan);
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				key = null;
				deviceMacAddress = null;
				name = null;

				properties.clear();

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					String qualifier = Bytes.toString(entry.getKey());

					switch (qualifier) {
					case "key":
						key = UUID.fromString(new String(entry.getValue(),
								StandardCharsets.UTF_8));
						break;
					case "name":
						name = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "macAddress":
						deviceMacAddress = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					default:
						properties.add(new KeyValuePair(qualifier, new String(
								entry.getValue(), StandardCharsets.UTF_8)));
					}
				}

				if ((deviceMacAddress != null)
						&& (deviceMacAddress.equals(macAddress))) {
					device = new AmphiroDevice(key, name, deviceMacAddress,
							properties);
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

		return device;
	}

	@Override
	public Device getUserWaterMeterDeviceBySerial(UUID userKey, String serial)
			throws Exception {
		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		WaterMeterDevice device = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);
			table = connection.getTable(TableName.valueOf(this.deviceTable));

			byte[] columnFamily = Bytes.toBytes(this.columnFamily);
			byte[] rowKey = md.digest(userKey.toString().getBytes("UTF-8"));

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			Filter prefixFilter = new PrefixFilter(rowKey);
			scan.setFilter(prefixFilter);

			UUID key = null;
			String deviceSerial = null;

			ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();

			scanner = table.getScanner(scan);
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				key = null;
				serial = null;

				properties.clear();

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					String qualifier = Bytes.toString(entry.getKey());

					switch (qualifier) {
					case "key":
						key = UUID.fromString(new String(entry.getValue(),
								StandardCharsets.UTF_8));
						break;
					case "serial":
						deviceSerial = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					default:
						properties.add(new KeyValuePair(qualifier, new String(
								entry.getValue(), StandardCharsets.UTF_8)));
					}
				}

				if ((deviceSerial != null) && (deviceSerial.equals(serial))) {
					device = new WaterMeterDevice(key, deviceSerial, properties);
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

		return device;
	}

	@Override
	public ArrayList<Device> getUserDevices(UUID userKey,
			DeviceRegistrationQuery query) throws Exception {
		ArrayList<Device> devices = new ArrayList<Device>();

		Connection connection = null;
		Table table = null;
		ResultScanner scanner = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);
			table = connection.getTable(TableName.valueOf(this.deviceTable));

			byte[] columnFamily = Bytes.toBytes(this.columnFamily);
			byte[] rowKey = md.digest(userKey.toString().getBytes("UTF-8"));

			Scan scan = new Scan();
			scan.addFamily(columnFamily);
			Filter prefixFilter = new PrefixFilter(rowKey);
			scan.setFilter(prefixFilter);

			Device device = null;

			UUID key = null;
			String serial = null;
			String macAddress = null;
			String name = null;
			EnumDeviceType type = EnumDeviceType.UNDEFINED;

			scanner = table.getScanner(scan);
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				device = null;

				key = null;
				serial = null;
				macAddress = null;
				name = null;
				type = EnumDeviceType.UNDEFINED;

				ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					String qualifier = Bytes.toString(entry.getKey());

					switch (qualifier) {
					case "type":
						type = EnumDeviceType.fromInteger(Bytes.toInt(entry
								.getValue()));
						break;
					case "key":
						key = UUID.fromString(new String(entry.getValue(),
								StandardCharsets.UTF_8));
						break;
					case "name":
						name = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "serial":
						name = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "macAddress":
						name = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					default:
						properties.add(new KeyValuePair(qualifier, new String(
								entry.getValue(), StandardCharsets.UTF_8)));
					}
				}

				if (key != null) {
					switch (type) {
					case METER:
						device = new WaterMeterDevice(key, serial, properties);
						break;
					case AMPHIRO:
						device = new AmphiroDevice(key, name, macAddress,
								properties);
						break;
					case UNDEFINED:
						break;
					}
					if (device == null) {
						continue;
					} else {
						if ((query.getType() != EnumDeviceType.UNDEFINED)
								&& (query.getType() != device.getType())) {
							continue;
						}
					}
					devices.add(device);
				}
			}

			return devices;
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
	}
}