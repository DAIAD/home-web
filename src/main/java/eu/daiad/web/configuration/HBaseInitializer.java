package eu.daiad.web.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import eu.daiad.web.hbase.HBaseConnectionManager;

@Component
public class HBaseInitializer implements CommandLineRunner {

	private static final Log logger = LogFactory.getLog(HBaseInitializer.class);

	@Autowired
	private HBaseConnectionManager connection;

	private final String namespace = "daiad";

	private final String tables[] = { "amphiro-sessions-index", "amphiro-sessions-by-time", "amphiro-sessions-by-user",
					"amphiro-measurements", "meter-measurements-by-time", "meter-measurements-by-user",
					"arduino-measurements", "amphiro-sessions-index-v2", "amphiro-sessions-by-time-v2",
					"amphiro-sessions-by-user-v2", "amphiro-measurements-v2" };

	private final String columnFamily = "cf";

	@Override
	public void run(String... args) throws Exception {
		Admin admin = null;

		try {
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
					admin = null;
				}
			} catch (Exception ex) {
				logger.error("Failed to release HBASE connection resources.", ex);
			}
		}
	}
}