package eu.daiad.scheduler.mapreduce;


/**
 * Enumeration of Hadoop parameters.
 */
public enum EnumHadoopParameter {
    /**
     * Not supported parameter.
     */
    NOT_SUPPORTED(""),
    /**
     * HDFS path.
     */
    HDFS_PATH("fs.defaultFS");

    private final String value;

    public String getValue() {
        return value;
    }

    private EnumHadoopParameter(String value) {
        this.value = value;
    }

    public static EnumHadoopParameter fromString(String value) {
        for (EnumHadoopParameter item : EnumHadoopParameter.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return NOT_SUPPORTED;
    }
}
