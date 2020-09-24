package eu.daiad.common.model.query;

import eu.daiad.common.model.device.EnumDeviceType;

/**
 * Enumerate measurement fields as legal combinations of (deviceType, field)
 */
public enum EnumMeasurementField
{
    METER_VOLUME(EnumDeviceType.METER, EnumDataField.VOLUME),
    
    AMPHIRO_VOLUME(EnumDeviceType.AMPHIRO, EnumDataField.VOLUME),
    AMPHIRO_DURATION(EnumDeviceType.AMPHIRO, EnumDataField.DURATION),
    AMPHIRO_FLOW(EnumDeviceType.AMPHIRO, EnumDataField.FLOW),
    AMPHIRO_ENERGY(EnumDeviceType.AMPHIRO, EnumDataField.ENERGY),
    AMPHIRO_TEMPERATURE(EnumDeviceType.AMPHIRO, EnumDataField.TEMPERATURE),
    ;
    
    private final EnumDeviceType deviceType;
    
    private final EnumDataField field;
    
    private EnumMeasurementField(EnumDeviceType deviceType, EnumDataField field)
    {
        this.deviceType = deviceType;
        this.field = field;
    }
    
    public EnumDeviceType getDeviceType()
    {
        return deviceType;
    }

    public EnumDataField getField()
    {
        return field;
    }

    public static EnumMeasurementField valueOf(EnumDeviceType deviceType, EnumDataField field)
    {
        for (EnumMeasurementField e: EnumMeasurementField.values()) {
            if (e.deviceType == deviceType && e.field == field)
                return e;
        }
        return null;
    }
}
