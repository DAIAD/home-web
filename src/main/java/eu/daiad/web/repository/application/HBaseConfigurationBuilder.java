package eu.daiad.web.repository.application;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.configuration.HBaseConnectionProperties;

@Component
public class HBaseConfigurationBuilder {

	@Autowired
	private HBaseConnectionProperties connectionProperties;

	public Configuration build() {
		Configuration config = HBaseConfiguration.create();
		
		config.set("hbase.zookeeper.quorum", this.connectionProperties.getHbaseZooKeeperQuorum());

		config.setInt("hbase.rpc.timeout", this.connectionProperties.getHbaseRpcTimeout());

		config.setInt("hbase.client.retries.number", this.connectionProperties.getHbaseClientRetriesNumber());
		config.setInt("hbase.client.pause", this.connectionProperties.getHbaseClientPause());

		config.setInt("zookeeper.recovery.retry", this.connectionProperties.getZookeeperRecoveryRetry());
		config.setInt("zookeeper.recovery.retry.intervalmill",
						this.connectionProperties.getZookeeperRecoveryRetryIntervalMillis());

		config.setInt("zookeeper.session.timeout", this.connectionProperties.getZookeeperSessionTimeout());

		return config;
	}

	public HBaseConfigurationBuilder setHbaseZooKeeperQuorum(String value) {
		this.connectionProperties.setHbaseZooKeeperQuorum(value);
		return this;
	}

	public HBaseConfigurationBuilder setHbaseRpcTimeout(int value) {
		this.connectionProperties.setHbaseRpcTimeout(value);
		return this;
	}

	public HBaseConfigurationBuilder setHbaseClientRetriesNumber(int value) {
		this.connectionProperties.setHbaseClientRetriesNumber(value);
		return this;
	}

	public HBaseConfigurationBuilder setHbaseClientPause(int value) {
		this.connectionProperties.setHbaseClientPause(value);
		return this;
	}

	public HBaseConfigurationBuilder setZookeeperRecoveryRetry(int value) {
		this.connectionProperties.setZookeeperRecoveryRetry(value);
		return this;
	}

	public HBaseConfigurationBuilder setZookeeperRecoveryRetryIntervalMillis(int value) {
		this.connectionProperties.setZookeeperRecoveryRetryIntervalMillis(value);
		return this;
	}

	public HBaseConfigurationBuilder setZookeeperSessionTimeout(int value) {
		this.connectionProperties.setZookeeperSessionTimeout(value);
		return this;
	}
}
