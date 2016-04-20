package eu.daiad.web.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class HBaseConnectionProperties {

	@Value("${hbase.zookeeper.quorum}")
	private String hbaseZooKeeperQuorum;

	@Value("${hbase.rpc.timeout}")
	private int hbaseRpcTimeout;

	@Value("${hbase.client.retries.number}")            
	private int hbaseClientRetriesNumber;

	@Value("${hbase.client.pause}")
	private int hbaseClientPause;

	@Value("${zookeeper.recovery.retry}")
	private int zookeeperRecoveryRetry;

	@Value("${zookeeper.recovery.retry.intervalmill}")
	private int zookeeperRecoveryRetryIntervalMillis;

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
