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

/**
 * Initializes HBase schema.
 */
@Component
public class HBaseInitializer implements CommandLineRunner {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(HBaseInitializer.class);

    /**
     * Wrapper to HBase connection.
     */
    @Autowired
    private HBaseConnectionManager connection;

    /**
     * Default namespace for all DAIAD HBase tables.
     */
    private static final String namespace = "daiad";

    /**
     * HBase tables required by DAIAD. If a table does not exist, it is created
     * when the application starts.
     */
    private static final String tables[] = { "counters",
                                             "amphiro-sessions-index",
                                             "amphiro-sessions-index-v2",
                                             "amphiro-sessions-index-v3",
                                             "amphiro-sessions-by-time",
                                             "amphiro-sessions-by-time-v2",
                                             "amphiro-sessions-by-time-v3",
                                             "amphiro-sessions-by-user",
                                             "amphiro-sessions-by-user-v2",
                                             "amphiro-sessions-by-user-v3",
                                             "amphiro-measurements",
                                             "amphiro-measurements-v2",
                                             "amphiro-measurements-v3",
                                             "meter-forecast-by-user",
                                             "meter-forecast-by-time",
                                             "meter-measurements-by-time",
                                             "meter-measurements-by-user",
                                             "meter-measurements-aggregate-by-time",
                                             "arduino-measurements",
                                             "comparison-ranking-daily"};

    /**
     * Default column family for all DAIAD HBase tables.
     */
    private static final String columnFamily = "cf";

    /**
     * On application start and after the Spring Application context is configured, this method creates
     * all missing HBase tables required by the application.
     */
    @Override
    public void run(String... args) throws Exception {
        Admin admin = null;

        try {
            if (connection.isAborted()) {
                throw new Exception("HBase: Connection has been aborted.");
            }
            if (connection.isClosed()) {
                throw new Exception("HBase: Connection is closed.");
            }

            admin = connection.getAdmin();

            boolean createNamespace = true;

            for (NamespaceDescriptor ns : admin.listNamespaceDescriptors()) {
                if (ns.getName().equals(namespace)) {
                    createNamespace = false;
                    break;
                }
            }

            if (createNamespace) {
                admin.createNamespace(NamespaceDescriptor.create(namespace).build());
            }

            for (String qualifier : tables) {
                String fullname = String.format("%s:%s", namespace, qualifier);

                TableName tableName = TableName.valueOf(fullname);

                if (!admin.tableExists(tableName)) {
                    HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);

                    tableDescriptor.addFamily(new HColumnDescriptor(columnFamily));

                    admin.createTable(tableDescriptor);
                    logger.info(String.format("HBase: Table [%s] has been created.", fullname));
                }
            }
        } catch (Exception ex) {
            logger.fatal("HBase: Failed to initialize schema.", ex);
        } finally {
            try {
                if (admin != null) {
                    admin.close();
                    admin = null;
                }
            } catch (Exception ex) {
                logger.error("HBase: Failed to release resources.", ex);
            }
        }
    }
}
