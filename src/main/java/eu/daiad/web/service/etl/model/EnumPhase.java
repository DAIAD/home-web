package eu.daiad.web.service.etl.model;

/**
 * Trial phases
 */
public enum EnumPhase {
    EMPTY,
    BASELINE,
    AMPHIRO_ON,
    AMPHIRO_ON_MOBILE_ON,
    AMPHIRO_ON_MOBILE_ON_SOCIAL_ON,
    MOBILE_ON,
    MOBILE_ON_AMPHIRO_ON,
    MOBILE_ON_AMPHIRO_ON_SOCIAL_ON,
    MOBILE_ON_SOCIAL_ON,
    MOBILE_ON_SOCIAL_ON_AMPHIRO_ON;

    public EnumPhase merge() {
        switch (this) {
            case BASELINE:
                return BASELINE;
            case AMPHIRO_ON:
                return AMPHIRO_ON;
            case AMPHIRO_ON_MOBILE_ON:
                return AMPHIRO_ON_MOBILE_ON;
            case AMPHIRO_ON_MOBILE_ON_SOCIAL_ON:
                return AMPHIRO_ON_MOBILE_ON_SOCIAL_ON;
            case MOBILE_ON:
                return MOBILE_ON;
            case MOBILE_ON_AMPHIRO_ON:
                return AMPHIRO_ON_MOBILE_ON;
            case MOBILE_ON_SOCIAL_ON:
                return MOBILE_ON_SOCIAL_ON;
            case MOBILE_ON_AMPHIRO_ON_SOCIAL_ON:
                return AMPHIRO_ON_MOBILE_ON_SOCIAL_ON;
            case MOBILE_ON_SOCIAL_ON_AMPHIRO_ON:
                return AMPHIRO_ON_MOBILE_ON_SOCIAL_ON;
            default:
                return EMPTY;
        }
    }
}
