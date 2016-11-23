package eu.daiad.web.model.message.insights;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.InsightBasicParameters;

public class InsightA1Parameters extends InsightBasicParameters
{
    public InsightA1Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double avgValue)
    {
        super(refDate, deviceType, currentValue, avgValue);
    }
    
    public int getDayOfWeek()
    {
        return refDate.getDayOfWeek();
    }

    @Override
    public EnumDynamicRecommendationType getType()
    {
        if (avgValue <= currentValue)
            return EnumDynamicRecommendationType.INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR;
        else 
            return EnumDynamicRecommendationType.INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR;
    }
    
    @Override
    public Map<String, Object> asParameters()
    {
        Map<String, Object> parameters = super.asParameters();
        
        parameters.put("day_of_week", Integer.valueOf(getDayOfWeek()));
        return parameters;
    }
}