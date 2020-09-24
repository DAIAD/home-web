package eu.daiad.common.repository.application;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.common.hbase.HBaseConnectionProperties;

/**
 * Builder for initializing HBase configuration.
 */
@Component
public class HBaseConfigurationBuilder {

    /**
     * HBase connection properties.
     */
    @Autowired
    private HBaseConnectionProperties connectionProperties;

    /**
     * Creates and configures HBase configuration object.
     *
     * @return the HBase configuration.
     */
    public Configuration build() {
        Configuration config = HBaseConfiguration.create();

        config.set("hbase.zookeeper.quorum", connectionProperties.getHbaseZooKeeperQuorum());

        config.setInt("hbase.rpc.timeout", connectionProperties.getHbaseRpcTimeout());

        config.setInt("hbase.client.retries.number", connectionProperties.getHbaseClientRetriesNumber());
        config.setInt("hbase.client.pause", connectionProperties.getHbaseClientPause());

        config.setInt("zookeeper.recovery.retry", connectionProperties.getZookeeperRecoveryRetry());
        config.setInt("zookeeper.recovery.retry.intervalmill", connectionProperties.getZookeeperRecoveryRetryIntervalMillis());

        config.setInt("zookeeper.session.timeout", connectionProperties.getZookeeperSessionTimeout());

        return config;
    }

    public HBaseConfigurationBuilder setHbaseZooKeeperQuorum(String value) {
        connectionProperties.setHbaseZooKeeperQuorum(value);
        return this;
    }

    public HBaseConfigurationBuilder setHbaseRpcTimeout(int value) {
        connectionProperties.setHbaseRpcTimeout(value);
        return this;
    }

    public HBaseConfigurationBuilder setHbaseClientRetriesNumber(int value) {
        connectionProperties.setHbaseClientRetriesNumber(value);
        return this;
    }

    public HBaseConfigurationBuilder setHbaseClientPause(int value) {
        connectionProperties.setHbaseClientPause(value);
        return this;
    }

    public HBaseConfigurationBuilder setZookeeperRecoveryRetry(int value) {
        connectionProperties.setZookeeperRecoveryRetry(value);
        return this;
    }

    public HBaseConfigurationBuilder setZookeeperRecoveryRetryIntervalMillis(int value) {
        connectionProperties.setZookeeperRecoveryRetryIntervalMillis(value);
        return this;
    }

    public HBaseConfigurationBuilder setZookeeperSessionTimeout(int value) {
        connectionProperties.setZookeeperSessionTimeout(value);
        return this;
    }
}
