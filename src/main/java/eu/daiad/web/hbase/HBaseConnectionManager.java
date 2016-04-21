package eu.daiad.web.hbase;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.repository.application.HBaseConfigurationBuilder;

@Component
public class HBaseConnectionManager implements InitializingBean, DisposableBean {

	private static final Log logger = LogFactory.getLog(HBaseConnectionManager.class);

	private Connection connection = null;

	@Autowired
	private HBaseConfigurationBuilder configurationBuilder;

	private synchronized void open() {
		if (this.connection == null) {
			try {
				Configuration config = this.configurationBuilder.build();
				this.connection = ConnectionFactory.createConnection(config);

				logger.info("HBASE connection has been opened.");
			} catch (Exception ex) {
				logger.fatal("Failed to initialize HBASE connection.", ex);
			}
		}
	}

	private synchronized void close() {
		if (this.connection != null) {
			try {
				this.connection.close();
				this.connection = null;

				logger.info("HBASE connection has been closed.");
			} catch (Exception ex) {
				logger.fatal("Failed to release HBASE connection resources.", ex);
			}
		}
	}

	public synchronized boolean isOpen() {
		return ((this.connection != null) && (!this.connection.isClosed()));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.open();
	}

	@Override
	public void destroy() throws Exception {
		this.close();
	}

	public Table getTable(String name) throws IOException {
		return this.connection.getTable(TableName.valueOf(name));
	}

	public Admin getAdmin() throws IOException {
		return this.connection.getAdmin();
	}

	public boolean isAborted() {
		if (this.connection == null) {
			return false;
		}
		return this.connection.isAborted();
	}

	public boolean isClosed() {
		if (this.connection == null) {
			return true;
		}
		return this.connection.isClosed();
	}
}
