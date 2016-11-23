package eu.daiad.web.model.message;

import java.io.Serializable;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonSerializable;

import eu.daiad.web.model.device.EnumDeviceType;

public interface IMessageParameters 
    //extends Serializable, JsonSerializable
{
    public EnumDynamicRecommendationType getType();
    
    public EnumDeviceType getDeviceType();
    
    public DateTime getReferenceDate();
    
    public Map<String, Object> asParameters();
    
}
