package eu.daiad.web.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
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

import eu.daiad.web.model.AmphiroDevice;
import eu.daiad.web.model.Device;
import eu.daiad.web.model.DeviceRegistrationQuery;
import eu.daiad.web.model.EnumDeviceType;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.WaterMeterDevice;

@Repository()
@Scope("prototype")
@PropertySource("${hbase.properties}")
public class DeviceRepository {

	private String quorum;

	private final String deviceTable = "daiad:device";

	private final String columnFamily = "cf";

	@Autowired
	public DeviceRepository(@Value("${hbase.zookeeper.quorum}") String quorum) {
		this.quorum = quorum;
	}

	public Device createAmphiroDevice(UUID userKey, String id, String name,
			ArrayList<KeyValuePair> properties) throws Exception {
		Connection connection = null;
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			if (this.getUserDeviceById(id, userKey) != null) {
				throw new Exception("Device already exists.");
			}
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			UUID devicekey = UUID.randomUUID();
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

			byte[] column = Bytes.toBytes("id");
			p.addColumn(columnFamily, column,
					id.getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("key");
			p.addColumn(columnFamily, column,
					devicekey.toString().getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("name");
			p.addColumn(columnFamily, column,
					name.getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("type");
			p.addColumn(columnFamily, column,
					Bytes.toBytes(EnumDeviceType.AMPHIRO.getValue()));

			if (properties != null) {
				for (int i = 0, count = properties.size(); i < count; i++) {
					column = Bytes.toBytes(properties.get(i).getKey());
					p.addColumn(columnFamily, column, properties.get(i)
							.getValue().getBytes(StandardCharsets.UTF_8));
				}
			}

			table.put(p);

			AmphiroDevice device = new AmphiroDevice(devicekey, id, name,
					properties);

			return device;
		} finally {
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}
	}

	public Device createMeterDevice(UUID userKey, String id,
			ArrayList<KeyValuePair> properties) throws Exception {
		Connection connection = null;
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			if (this.getUserDeviceById(id, userKey) != null) {
				throw new Exception("Device already exists.");
			}
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			UUID devicekey = UUID.randomUUID();
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

			byte[] column = Bytes.toBytes("id");
			p.addColumn(columnFamily, column,
					id.getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("key");
			p.addColumn(columnFamily, column,
					devicekey.toString().getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("type");
			p.addColumn(columnFamily, column,
					Bytes.toBytes(EnumDeviceType.METER.getValue()));

			if (properties != null) {
				for (int i = 0, count = properties.size(); i < count; i++) {
					column = Bytes.toBytes(properties.get(i).getKey());
					p.addColumn(columnFamily, column, properties.get(i)
							.getValue().getBytes(StandardCharsets.UTF_8));
				}
			}

			table.put(p);

			WaterMeterDevice device = new WaterMeterDevice(devicekey, id,
					properties);

			return device;
		} finally {
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}
	}

	public Device getUserDeviceByKey(UUID key, UUID userKey) throws Exception {
		Device device = null;

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

			String deviceId = null;
			UUID deviceKey = null;
			String name = null;
			EnumDeviceType type = EnumDeviceType.UNDEFINED;
			ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();

			scanner = table.getScanner(scan);
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				deviceId = null;
				deviceKey = null;
				name = null;
				type = EnumDeviceType.UNDEFINED;
				properties.clear();

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					String qualifier = Bytes.toString(entry.getKey());

					switch (qualifier) {
					case "id":
						deviceId = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "key":
						deviceKey = UUID.fromString(new String(
								entry.getValue(), StandardCharsets.UTF_8));
						break;
					case "name":
						name = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "type":
						type = EnumDeviceType.fromInteger(Bytes.toInt(entry
								.getValue()));
						break;
					default:
						properties.add(new KeyValuePair(qualifier, new String(
								entry.getValue(), StandardCharsets.UTF_8)));
					}
				}

				if ((deviceKey != null) && (deviceKey.equals(key))) {
					break;
				}
			}

			if ((deviceKey != null) && (deviceKey.equals(key))) {
				switch (type) {
				case METER:
					device = new WaterMeterDevice(deviceKey, deviceId,
							properties);
					break;
				case AMPHIRO:
					device = new AmphiroDevice(deviceKey, deviceId, name,
							properties);
					break;
				case UNDEFINED:
					break;
				}
			}

			return device;
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

	public Device getUserDeviceById(String id, UUID userKey) throws Exception {
		Device device = null;

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

			String deviceId = null;
			UUID deviceKey = null;
			String name = null;
			EnumDeviceType type = EnumDeviceType.UNDEFINED;
			ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();

			scanner = table.getScanner(scan);
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				deviceId = null;
				deviceKey = null;
				name = null;
				type = EnumDeviceType.UNDEFINED;
				properties.clear();

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					String qualifier = Bytes.toString(entry.getKey());

					switch (qualifier) {
					case "id":
						deviceId = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "key":
						deviceKey = UUID.fromString(new String(
								entry.getValue(), StandardCharsets.UTF_8));
						break;
					case "name":
						name = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "type":
						type = EnumDeviceType.fromInteger(Bytes.toInt(entry
								.getValue()));
						break;
					default:
						properties.add(new KeyValuePair(qualifier, new String(
								entry.getValue(), StandardCharsets.UTF_8)));
					}
				}

				if ((deviceId != null) && (deviceId.equals(id))) {
					break;
				}
			}

			if ((deviceId != null) && (deviceId.equals(id))) {
				switch (type) {
				case METER:
					device = new WaterMeterDevice(deviceKey, deviceId,
							properties);
					break;
				case AMPHIRO:
					device = new AmphiroDevice(deviceKey, deviceId, name,
							properties);
					break;
				case UNDEFINED:
					break;
				}
			}

			return device;
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

			String deviceId = null;
			UUID deviceKey = null;
			String name = null;
			EnumDeviceType type = EnumDeviceType.UNDEFINED;

			scanner = table.getScanner(scan);
			for (Result r = scanner.next(); r != null; r = scanner.next()) {
				NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

				device = null;

				deviceId = null;
				deviceKey = null;
				name = null;
				type = EnumDeviceType.UNDEFINED;
				ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();

				for (Entry<byte[], byte[]> entry : map.entrySet()) {
					String qualifier = Bytes.toString(entry.getKey());

					switch (qualifier) {
					case "id":
						deviceId = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "key":
						deviceKey = UUID.fromString(new String(
								entry.getValue(), StandardCharsets.UTF_8));
						break;
					case "name":
						name = new String(entry.getValue(),
								StandardCharsets.UTF_8);
						break;
					case "type":
						type = EnumDeviceType.fromInteger(Bytes.toInt(entry
								.getValue()));
						break;
					default:
						properties.add(new KeyValuePair(qualifier, new String(
								entry.getValue(), StandardCharsets.UTF_8)));
					}
				}

				if (deviceKey != null) {
					switch (type) {
					case METER:
						device = new WaterMeterDevice(deviceKey, deviceId,
								properties);
						break;
					case AMPHIRO:
						device = new AmphiroDevice(deviceKey, deviceId, name,
								properties);
						break;
					case UNDEFINED:
						break;
					}
					if (device == null) {
						continue;
					}
					if (query != null) {
						if ((!StringUtils.isBlank(query.getDeviceId()))
								&& (!query.getDeviceId().equals(
										device.getDeviceId()))) {
							continue;
						}
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