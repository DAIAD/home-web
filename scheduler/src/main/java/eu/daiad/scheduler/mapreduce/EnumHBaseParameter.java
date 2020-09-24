package eu.daiad.scheduler.mapreduce;

/**
 * Enumeration of HBase parameters.
 */
public enum EnumHBaseParameter {
    /**
     * Not supported parameter.
     */
    NOT_SUPPORTED(""),
    /**
     * Comma separated list of servers in the ZooKeeper ensemble.
     */
    JOB_NAME("hbase.zookeeper.quorum"),
    /**
     * Number of rows that we try to fetch when calling next on a scanner if it
     * is not served from (local, client) memory.
     */
    JAR_NAME("hbase.client.scanner.caching");

    private final String value;

    public String getValue() {
        return value;
    }

    private EnumHBaseParameter(String value) {
        this.value = value;
    }

    public static EnumHBaseParameter fromString(String value) {
        for (EnumHBaseParameter item : EnumHBaseParameter.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return NOT_SUPPORTED;
    }
}
