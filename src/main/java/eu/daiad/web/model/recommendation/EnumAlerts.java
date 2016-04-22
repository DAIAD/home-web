package eu.daiad.web.model.recommendation;

/**
 *
 * @author nkarag
 */
public enum EnumAlerts {
    
    //changes here, affect the database. Chang alert ids in sql script accordingly.
    WATER_LEAK(1),
    SHOWER_ON(2),
    WATER_FIXTURES(3),
    //WATER_FIXTURES ignored, requires SWM with high granularity.
    UNUSUAL_ACTIVITY(4), //ignored, until we have consumption patterns
    WATER_QUALITY(5),
    HOT_TEMPERATURE(6),
    NEAR_DAILY_WATER_BUDGET(7),
    NEAR_WEEKLY_WATER_BUDGET(8),
    NEAR_DAILY_SHOWER_BUDGET(9),
    NEAR_WEEKLY_SHOWER_BUDGET(10),
    REACHED_DAILY_WATER_BUDGET(11),
    REACHED_DAILY_SHOWER_BUDGET(12),
    WATER_CHAMPION(13),
    SHOWER_CHAMPION(14),   
    TOO_MUCH_WATER_SWM(15),
    TOO_MUCH_WATER_AMPHIRO(16),
    TOO_MUCH_ENERGY(17),
    REDUCED_WATER_USE(18),
    IMPROVED_SHOWER_EFFICIENCY(19),
    WATER_EFFICIENCY_LEADER(20),
    KEEP_UP_SAVING_WATER(21),
    GOOD_JOB_MONTHLY(22),
    //GOOD_JOB_WEEKLY(23),
    LITERS_ALREADY_SAVED(23),
    TOP_25_PERCENT_OF_SAVERS(24),
    TOP_10_PERCENT_OF_SAVERS(25),
    DID_YOU_KNOW1(26),
    DID_YOU_KNOW2(27),
    DID_YOU_KNOW3(28);

    private final int value;
    private static final String TYPE = "alert";
    
    private EnumAlerts(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
    
    public static String getType(){
        return TYPE;
    }
}
