package eu.daiad.common.hbase;

import java.util.HashMap;
import java.util.Map;

/**
 * HBase tables required by DAIAD
 */
public enum EnumHBaseTable {
    /**
     * Stores application counters used for generating unique row keys and column qualifiers
     */
    COUNTERS("daiad:counters"),
    /**
     * Index for amphiro real-time showers (schema version 1)
     */
    AMPHIRO_SHOWER_INDEX_V1("daiad:amphiro-sessions-index"),
    /**
     * Index for amphiro real-time showers (schema version 2)
     */
    AMPHIRO_SHOWER_INDEX_V2("daiad:amphiro-sessions-index-v2"),
    /**
     * Index for amphiro real-time showers (schema version 3)
     */
    AMPHIRO_SHOWER_INDEX_V3("daiad:amphiro-sessions-index-v3"),
    /**
     * Stores amphiro showers indexed by time (schema version 1)
     */
    AMPHIRO_SHOWER_TIME_V1("daiad:amphiro-sessions-by-time"),
    /**
     * Stores amphiro showers indexed by time (schema version 2)
     */
    AMPHIRO_SHOWER_TIME_V2("daiad:amphiro-sessions-by-time-v2"),
    /**
     * Stores amphiro showers indexed by time (schema version 3)
     */
    AMPHIRO_SHOWER_TIME_V3("daiad:amphiro-sessions-by-time-v3"),
    /**
     * Stores amphiro showers indexed by user (schema version 1)
     */
    AMPHIRO_SHOWER_USER_V1("daiad:amphiro-sessions-by-user"),
    /**
     * Stores amphiro showers indexed by user (schema version 2)
     */
    AMPHIRO_SHOWER_USER_V2("daiad:amphiro-sessions-by-user-v2"),
    /**
     * Stores amphiro showers indexed by user (schema version 3)
     */
    AMPHIRO_SHOWER_USER_V3("daiad:amphiro-sessions-by-user-v3"),
    /**
     * Stores amphiro shower time series (schema version 1)
     */
    AMPHIRO_SHOWER_TIME_SERIES_V1("daiad:amphiro-measurements"),
    /**
     * Stores amphiro shower time series (schema version 2)
     */
    AMPHIRO_SHOWER_TIME_SERIES_V2("daiad:amphiro-measurements-v2"),
    /**
     * Stores amphiro shower time series (schema version 3)
     */
    AMPHIRO_SHOWER_TIME_SERIES_V3("daiad:amphiro-measurements-v3"),
    /**
     * Stores smart water meter forecasting data indexed by user
     */
    SWM_FORECAST_USER("daiad:meter-forecast-by-user"),
    /**
     * Stores smart water meter forecasting data indexed by time
     */
    SWM_FORECAST_TIME("daiad:meter-forecast-by-time"),
    /**
     * Stores smart water meter forecasting data aggregates indexed by meter, utility, group, set, segment and commons
     */
    SWM_FORECAST_AGGREGATE("daiad:meter-forecast-aggregate"),
    /**
     * Stores smart water meter data indexed by user
     */
    SWM_USER("daiad:meter-measurements-by-user"),
    /**
     * Stores smart water meter data indexed by time
     */
    SWM_TIME("daiad:meter-measurements-by-time"),
    /**
     * Stores smart water meter data aggregates indexed by meter, utility, group, set, segment and commons
     */
    SWM_AGGREGATE("daiad:meter-measurements-aggregate"),
    /**
     * Stores measurements collected by arduino appliances
     */
    ARDUINO_TIME_SERIES("daiad:arduino-measurements"),
    /**
     * Stores daily comparison and ranking data for every user
     */
    COMPARISON_RANKING_DAILY("daiad:comparison-ranking-daily");

    private final String value;

    private static final Map<String, EnumHBaseTable> stringToEnumMap = new HashMap<String, EnumHBaseTable>();
    static {
        for (EnumHBaseTable type : EnumHBaseTable.values()) {
            if (stringToEnumMap.containsKey(type.value)) {
                throw new IllegalArgumentException(String.format("Duplicate constant found for table [%s] and values [%s], [%s].",
                                                                 type.value,
                                                                 type,
                                                                 stringToEnumMap.get(type.value)));
            } else {
                stringToEnumMap.put(type.value, type);
            }
        }
    }

    private EnumHBaseTable(String value) {
        this.value = value;
    }

    public String getValue() {
        return value.toLowerCase();
    }

    public static EnumHBaseTable fromString(String value) {
        for (EnumHBaseTable item : EnumHBaseTable.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException(String.format("HBase table [%s] is not registered.", value));
    }

}
