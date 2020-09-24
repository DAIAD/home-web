package eu.daiad.common.hbase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Contains configuration properties for HBASE.
 */
@Component
public final class HBaseConnectionProperties {

    /**
     * Comma separated list of servers in the ZooKeeper ensemble
     */
    @Value("${hbase.zookeeper.quorum}")
    private String hbaseZooKeeperQuorum;

    /**
     * This is for the RPC layer to define how long HBase client applications
     * take for a remote call to time out. It uses pings to check connections
     * but will eventually throw a  {@link org.apache.hadoop.hbase.errorhandling.TimeoutException}.
     */
    @Value("${hbase.rpc.timeout}")
    private int hbaseRpcTimeout;

    /**
     * Maximum retries. Used as maximum for all retryable operations such as the
     * getting of a cell's value, starting a row update, etc. Retry interval is
     * a rough function based on hbase.client.pause.
     */
    @Value("${hbase.client.retries.number}")
    private int hbaseClientRetriesNumber;

    /**
     * General client pause value. Used mostly as value to wait before running a
     * retry of a failed get, region lookup, etc.
     */
    @Value("${hbase.client.pause}")
    private int hbaseClientPause;

    /**
     * Zookeeper retry count
     */
    @Value("${zookeeper.recovery.retry}")
    private int zookeeperRecoveryRetry;

    /**
     * Zookeeper retry wait
     */
    @Value("${zookeeper.recovery.retry.intervalmill}")
    private int zookeeperRecoveryRetryIntervalMillis;

    /**
     * ZooKeeper session timeout in milliseconds. It is used in two different
     * ways. First, this value is used in the ZK client that HBase uses to
     * connect to the ensemble. It is also used by HBase when it starts a ZK
     * server and it is passed as the 'maxSessionTimeout'.
     */
    @Value("${zookeeper.session.timeout}")
    private int zookeeperSessionTimeout;

    public String getHbaseZooKeeperQuorum() {
        return hbaseZooKeeperQuorum;
    }

    public void setHbaseZooKeeperQuorum(String hbaseZooKeeperQuorum) {
        this.hbaseZooKeeperQuorum = hbaseZooKeeperQuorum;
    }

    public int getHbaseRpcTimeout() {
        return hbaseRpcTimeout;
    }

    public void setHbaseRpcTimeout(int hbaseRpcTimeout) {
        this.hbaseRpcTimeout = hbaseRpcTimeout;
    }

    public int getHbaseClientRetriesNumber() {
        return hbaseClientRetriesNumber;
    }

    public void setHbaseClientRetriesNumber(int hbaseClientRetriesNumber) {
        this.hbaseClientRetriesNumber = hbaseClientRetriesNumber;
    }

    public int getHbaseClientPause() {
        return hbaseClientPause;
    }

    public void setHbaseClientPause(int hbaseClientPause) {
        this.hbaseClientPause = hbaseClientPause;
    }

    public int getZookeeperRecoveryRetry() {
        return zookeeperRecoveryRetry;
    }

    public void setZookeeperRecoveryRetry(int zookeeperRecoveryRetry) {
        this.zookeeperRecoveryRetry = zookeeperRecoveryRetry;
    }

    public int getZookeeperRecoveryRetryIntervalMillis() {
        return zookeeperRecoveryRetryIntervalMillis;
    }

    public void setZookeeperRecoveryRetryIntervalMillis(int zookeeperRecoveryRetryIntervalMillis) {
        this.zookeeperRecoveryRetryIntervalMillis = zookeeperRecoveryRetryIntervalMillis;
    }

    public int getZookeeperSessionTimeout() {
        return zookeeperSessionTimeout;
    }

    public void setZookeeperSessionTimeout(int zookeeperSessionTimeout) {
        this.zookeeperSessionTimeout = zookeeperSessionTimeout;
    }

}
