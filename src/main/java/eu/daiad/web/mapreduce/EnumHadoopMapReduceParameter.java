package eu.daiad.web.mapreduce;

/**
 * Enumeration of MapReduce parameters.
 */
public enum EnumHadoopMapReduceParameter {
    /**
     * Not supported parameter.
     */
    NOT_SUPPORTED(""),
    /**
     * The runtime framework for executing MapReduce jobs. Can be one of local,
     * classic or yarn.
     */
    FRAMEWORK_NAME("mapreduce.framework.name"),
    /**
     * If true, then multiple instances of some map tasks may be executed in
     * parallel.
     */
    MAP_SPECULATIVE("mapreduce.map.speculative");

    private final String value;

    public String getValue() {
        return value;
    }

    private EnumHadoopMapReduceParameter(String value) {
        this.value = value;
    }

    public static EnumHadoopMapReduceParameter fromString(String value) {
        for (EnumHadoopMapReduceParameter item : EnumHadoopMapReduceParameter.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return NOT_SUPPORTED;
    }
}
