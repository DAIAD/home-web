package eu.daiad.web.mapreduce;

/**
 * Enumeration of YARN parameters.
 */
public enum EnumYarnParameter {
    /**
     * Not supported parameter.
     */
    NOT_SUPPORTED(""),
    /**
     * The host name of the RM.
     */
    RESOURCE_MANAGER_HOSTNAME("yarn.resourcemanager.hostname"),
    /**
     * A comma separated list of services where service name should only contain
     * a-zA-Z0-9_ and can not start with numbers.
     */
    NODE_MANAGER_AUX_SERVICES("yarn.nodemanager.aux-services");

    private final String value;

    public String getValue() {
        return value;
    }

    private EnumYarnParameter(String value) {
        this.value = value;
    }

    public static EnumYarnParameter fromString(String value) {
        for (EnumYarnParameter item : EnumYarnParameter.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return NOT_SUPPORTED;
    }
}
