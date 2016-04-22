package eu.daiad.web.model.recommendation;

/**
 *
 * @author nkarag
 */
public enum EnumDynamicRecommendations {
    LESS_SHOWER_TIME(1),
    LOWER_TEMPERATURE(2),
    LOWER_FLOW(3),
    CHANGE_SHOWERHEAD(4),
    SHAMPOO_CHANGE(5),
    REDUCE_FLOW_WHEN_NOT_NEEDED(6);
    
    private final int value;
    private static final String TYPE = "recommendation";
    
    private EnumDynamicRecommendations(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }  
    
    public static String getType(){
        return TYPE;
    }    
}
