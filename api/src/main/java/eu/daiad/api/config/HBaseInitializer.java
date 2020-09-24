package eu.daiad.api.config;

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

import eu.daiad.common.hbase.EnumHBaseColumnFamily;
import eu.daiad.common.hbase.EnumHBaseNamespace;
import eu.daiad.common.hbase.EnumHBaseTable;
import eu.daiad.common.hbase.HBaseConnectionManager;

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
     * On application start and after the Spring Application context is
     * configured, this method creates all missing HBase tables required by the
     * application.
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

            NamespaceDescriptor[] namespaceDescriptor = admin.listNamespaceDescriptors();
            for (EnumHBaseNamespace namespace : EnumHBaseNamespace.values()) {
                boolean createNamespace = true;

                for (NamespaceDescriptor ns : namespaceDescriptor) {
                    if (ns.getName().equalsIgnoreCase(namespace.getValue())) {
                        createNamespace = false;
                        break;
                    }
                }

                if (createNamespace) {
                    admin.createNamespace(NamespaceDescriptor.create(namespace.getValue()).build());
                }
            }

            for (EnumHBaseTable table : EnumHBaseTable.values()) {
                TableName tableName = TableName.valueOf(table.getValue());

                if (!admin.tableExists(tableName)) {
                    HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);

                    tableDescriptor.addFamily(new HColumnDescriptor(EnumHBaseColumnFamily.DEFAULT.getValue()));

                    admin.createTable(tableDescriptor);
                    logger.info(String.format("HBase: Table [%s] has been created.", table.getValue()));
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
