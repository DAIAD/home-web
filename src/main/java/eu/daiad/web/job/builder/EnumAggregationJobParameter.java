package eu.daiad.web.job.builder;

/**
 * Enumeration of custom MapReduce job parameters.
 */
public enum EnumAggregationJobParameter {
    /**
     * Not supported parameter.
     */
    NOT_SUPPORTED(""),
    /**
     * HBase input table.
     */
    INPUT_TABLE("daiad.hbase.table.input"),
    /**
     * HBase output table
     */
    OUTPUT_TABLE("daiad.hbase.table.output"),
    /**
     * Default column family for input/output tables.
     */
    COLUMN_FAMILY("daiad.hbase.column-family"),
    /**
     * Number of partitions.
     */
    PARTITIONS("daiad.hbase.data.partitions"),
    /**
     * Date interval format.
     */
    DATE_FORMAT("daiad.interval.format"),
    /**
     * Date interval start instant.
     */
    DATE_FROM("daiad.interval.from"),
    /**
     * Date interval end instant.
     */
    DATE_TO("daiad.interval.to"),
    /**
     * Top-k / Bottom-k query limit.
     */
    TOP_QUERY_LIMIT("daiad.top.query.limit"),
    /**
     * Date interval end instant.
     */
    FILENAME_GROUPS("daiad.filename.groups"),
    /**
     * Date interval end instant.
     */
    FILENAME_USERS("daiad.filename.users");

    private final String value;

    public String getValue() {
        return value;
    }

    private EnumAggregationJobParameter(String value) {
        this.value = value;
    }

    public static EnumAggregationJobParameter fromString(String value) {
        for (EnumAggregationJobParameter item : EnumAggregationJobParameter.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return NOT_SUPPORTED;
    }
}