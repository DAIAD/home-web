package eu.daiad.utility.service.etl.model;

/**
 * Possible state transitions e.g. configuration or profile update.
 */
public enum EnumTransition {
    /**
     * Initial amphiro b1 state. This is the default OFF configuration
     * assigned to a device when it is paired for the first time.
     */
    AMPHIRO_PAIRED,
    /**
     * Amphiro b1 has been enabled.
     */
    AMPHIRO_ON,
    /**
     * Mobile application has been enabled.
     */
    MOBILE_ON,
    /**
     * Social mode enabled
     */
    SOCIAL_ON;
}