package eu.daiad.web.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.EnumGender;
import eu.daiad.web.security.model.ApplicationUser;
import eu.daiad.web.security.model.EnumRole;

@Repository()
@Scope("prototype")
@PropertySource("${hbase.properties}")
public class HBaseUserRepository implements IUserRepository {

	private static final Log logger = LogFactory
			.getLog(HBaseUserRepository.class);

	private String quorum;

	private final String userTable = "daiad:user";

	private final String columnFamily = "cf";

	private final String defaultAdminUsername = "Administrator";

	private final EnumRole[] defaultAdminRoles = { EnumRole.ROLE_USER,
			EnumRole.ROLE_ADMIN };

	@Autowired
	public HBaseUserRepository(@Value("${hbase.zookeeper.quorum}") String quorum) {
		this.quorum = quorum;
	}

	@Override
	public void createDefaultUser() {
		Connection connection = null;
		Table table = null;

		String password = UUID.randomUUID().toString();

		try {
			if (this.getUserByName(this.defaultAdminUsername) != null) {
				return;
			}

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.userTable));

			byte[] columnFamily = Bytes.toBytes(this.columnFamily);
			byte[] rowKey = md.digest(this.defaultAdminUsername
					.getBytes(StandardCharsets.UTF_8));

			Put p = new Put(rowKey);

			byte[] column = Bytes.toBytes("key");
			p.addColumn(columnFamily, column, UUID.randomUUID().toString()
					.getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("username");
			p.addColumn(columnFamily, column,
					this.defaultAdminUsername.getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("password");
			p.addColumn(columnFamily, column, encoder.encode(password)
					.getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("firstname");
			p.addColumn(columnFamily, column,
					"Administrator".getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("timezone");
			p.addColumn(columnFamily, column,
					"Europe/Athens".getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("roles");
			p.addColumn(
					columnFamily,
					column,
					StringUtils.join(defaultAdminRoles, ",").getBytes(
							StandardCharsets.UTF_8));

			table.put(p);

			logger.warn(String
					.format("Default administrator user has been crearted. User name : %s. Password : %s",
							this.defaultAdminUsername, password));
		} catch (Exception ex) {
			logger.error("Failed to create default administartor user.", ex);
		} finally {
			try {
				if (table != null) {
					table.close();
				}
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error("Failed to release resources.", ex);
			}
		}
	}

	@Override
	public ApplicationUser createUser(String username, String password,
			String firstname, String lastname, EnumGender gender,
			DateTime birthdate, String country, String postalCode,
			String timezone) throws Exception {
		Connection connection = null;
		Table table = null;

		try {
			if (username.equals(this.defaultAdminUsername)) {
				throw new Exception("Username is reserved.");
			}
			if (this.getUserByName(username) != null) {
				throw new Exception("User already exists.");
			}
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			UUID userKey = UUID.randomUUID();

			table = connection.getTable(TableName.valueOf(this.userTable));
			byte[] columnFamily = Bytes.toBytes(this.columnFamily);

			byte[] rowKey = md
					.digest(username.getBytes(StandardCharsets.UTF_8));

			Put p = new Put(rowKey);

			byte[] column = Bytes.toBytes("key");
			p.addColumn(columnFamily, column,
					userKey.toString().getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("username");
			p.addColumn(columnFamily, column,
					username.getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("password");
			p.addColumn(columnFamily, column, encoder.encode(password)
					.getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("firstname");
			p.addColumn(columnFamily, column,
					firstname.getBytes(StandardCharsets.UTF_8));
			column = Bytes.toBytes("lastname");
			p.addColumn(columnFamily, column,
					lastname.getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("gender");
			p.addColumn(columnFamily, column, Bytes.toBytes(gender.getValue()));

			if (birthdate != null) {
				column = Bytes.toBytes("birthdate");
				p.addColumn(columnFamily, column,
						Bytes.toBytes(birthdate.getMillis()));
			}

			column = Bytes.toBytes("country");
			p.addColumn(columnFamily, column,
					country.getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("postalCode");
			p.addColumn(columnFamily, column,
					postalCode.getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("timezone");
			p.addColumn(columnFamily, column,
					timezone.getBytes(StandardCharsets.UTF_8));

			column = Bytes.toBytes("roles");
			p.addColumn(columnFamily, column, EnumRole.ROLE_USER.toString()
					.getBytes(StandardCharsets.UTF_8));

			table.put(p);

			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			authorities.add(new SimpleGrantedAuthority(EnumRole.ROLE_USER
					.toString()));

			ApplicationUser user = new ApplicationUser(userKey, username,
					encoder.encode(password), authorities);
			user.setFirstname(firstname);
			user.setLastname(lastname);
			user.setBirthdate(birthdate);
			user.setCountry(country);
			user.setGender(gender);
			user.setPostalCode(postalCode);

			return user;
		} finally {
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}
	}

	@Override
	public void setPassword(String username, String password) throws Exception {
		Connection connection = null;
		Table table = null;

		try {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.userTable));
			byte[] columnFamily = Bytes.toBytes(this.columnFamily);

			byte[] rowKey = md
					.digest(username.getBytes(StandardCharsets.UTF_8));

			Put p = new Put(rowKey);

			byte[] column = Bytes.toBytes("password");
			p.addColumn(columnFamily, column, encoder.encode(password)
					.getBytes(StandardCharsets.UTF_8));

			table.put(p);
		} finally {
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}
	}

	@Override
	public void setRole(String username, EnumRole role, boolean set)
			throws Exception {
		Connection connection = null;
		Table table = null;

		try {
			ApplicationUser user = this.getUserByName(username);

			ArrayList<EnumRole> roles = new ArrayList<EnumRole>();
			Iterator<GrantedAuthority> it = user.getAuthorities().iterator();
			while (it.hasNext()) {
				GrantedAuthority authority = (GrantedAuthority) it.next();

				roles.add(EnumRole.fromString(authority.toString()));
			}
			roles.remove(role);
			if (set) {
				roles.add(role);
			}

			Configuration config = HBaseConfiguration.create();

			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);

			MessageDigest md = MessageDigest.getInstance("MD5");

			table = connection.getTable(TableName.valueOf(this.userTable));
			byte[] columnFamily = Bytes.toBytes(this.columnFamily);

			byte[] rowKey = md
					.digest(username.getBytes(StandardCharsets.UTF_8));

			Put p = new Put(rowKey);

			byte[] column = Bytes.toBytes("roles");
			p.addColumn(columnFamily, column, StringUtils.join(roles, ",")
					.getBytes(StandardCharsets.UTF_8));

			table.put(p);
		} finally {
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}
	}

	@Override
	public ApplicationUser getUserByName(String username) throws Exception {
		Connection connection = null;
		Table table = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			Configuration config = HBaseConfiguration.create();
			config.set("hbase.zookeeper.quorum", this.quorum);

			connection = ConnectionFactory.createConnection(config);
			table = connection.getTable(TableName.valueOf(this.userTable));

			byte[] columnFamily = Bytes.toBytes(this.columnFamily);
			byte[] rowKey = md.digest(username.getBytes("UTF-8"));

			Get g = new Get(rowKey);

			Result r = table.get(g);
			NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

			if (map == null) {
				return null;
			}

			UUID id = null;
			String password = null;
			String firstname = null;
			String lastname = null;
			EnumGender gender = null;
			DateTime birthdate = null;
			String country = null;
			String postalCode = null;
			String timezone = "Europe/Athens";
			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

			for (Entry<byte[], byte[]> entry : map.entrySet()) {
				String qualifier = Bytes.toString(entry.getKey());

				switch (qualifier) {
				case "key":
					id = UUID.fromString(new String(entry.getValue(),
							StandardCharsets.UTF_8));
					break;
				case "firstname":
					firstname = new String(entry.getValue(),
							StandardCharsets.UTF_8);
					break;
				case "lastname":
					lastname = new String(entry.getValue(),
							StandardCharsets.UTF_8);
					break;
				case "country":
					country = new String(entry.getValue(),
							StandardCharsets.UTF_8);
					break;
				case "postalCode":
					postalCode = new String(entry.getValue(),
							StandardCharsets.UTF_8);
					break;
				case "birthdate":
					birthdate = new DateTime(Bytes.toLong(entry.getValue()));
					break;
				case "gender":
					gender = EnumGender.fromInteger(Bytes.toInt(entry
							.getValue()));
					break;
				case "password":
					password = new String(entry.getValue(),
							StandardCharsets.UTF_8);
					break;
				case "timezone":
					timezone = new String(entry.getValue(),
							StandardCharsets.UTF_8);
					break;
				case "roles":
					String[] roles = (new String(entry.getValue(),
							StandardCharsets.UTF_8)).split(",");
					for (int index = 0, count = roles.length; index < count; index++) {
						authorities
								.add(new SimpleGrantedAuthority(roles[index]));
					}
					break;
				}
			}

			table.close();
			connection.close();

			ApplicationUser user = new ApplicationUser(id, username, password,
					authorities);
			user.setFirstname(firstname);
			user.setLastname(lastname);
			user.setBirthdate(birthdate);
			user.setCountry(country);
			user.setGender(gender);
			user.setPostalCode(postalCode);
			user.setTimezone(timezone);

			return user;
		} finally {
			if (table != null) {
				table.close();
			}
			if ((connection != null) && (!connection.isClosed())) {
				connection.close();
			}
		}
	}

}