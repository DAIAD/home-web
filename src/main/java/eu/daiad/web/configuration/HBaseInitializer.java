package eu.daiad.web.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import eu.daiad.web.repository.application.HBaseConfigurationBuilder;

@Component
public class HBaseInitializer implements CommandLineRunner {

	private static final Log logger = LogFactory.getLog(HBaseInitializer.class);

	@Autowired
	private HBaseConfigurationBuilder configurationBuilder;

	private final String namespace = "daiad";

	private final String tables[] = { "amphiro-measurements", "amphiro-sessions-by-time", "amphiro-sessions-by-user",
					"meter-measurements-by-time", "meter-measurements-by-user", "arduino-measurements" };

	private final String columnFamily = "cf";

	@Override
	public void run(String... args) throws Exception {
		Connection connection = null;
		Admin admin = null;

		try {
			Configuration config = this.configurationBuilder.build();
			connection = ConnectionFactory.createConnection(config);

			Thread.sleep(60000);

			if (connection.isAborted()) {
				throw new Exception("aborted");
			}
			if (connection.isClosed()) {
				throw new Exception("closed");
			}

			admin = connection.getAdmin();

			boolean createNamespace = true;

			for (NamespaceDescriptor ns : admin.listNamespaceDescriptors()) {
				if (ns.getName().equals(this.namespace)) {
					createNamespace = false;
					break;
				}
			}

			if (createNamespace) {
				admin.createNamespace(NamespaceDescriptor.create(this.namespace).build());
			}

			for (String qualifier : this.tables) {
				String fullname = String.format("%s:%s", this.namespace, qualifier);

				TableName tableName = TableName.valueOf(fullname);

				if (!admin.tableExists(tableName)) {
					HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);

					tableDescriptor.addFamily(new HColumnDescriptor(this.columnFamily));

					admin.createTable(tableDescriptor);
					logger.info(String.format("HBASE table [%s] has been created.", fullname));
				}
			}
		} catch (Exception ex) {
			logger.fatal("Failed to create HBASE schema.", ex);
		} finally {
			try {
				if (admin != null) {
					admin.close();
				}
				if ((connection != null) && (!connection.isClosed())) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error("Failed to release HBASE connection resources.", ex);
			}
		}
	}
}