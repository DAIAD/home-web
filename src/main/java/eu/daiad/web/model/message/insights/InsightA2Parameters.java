package eu.daiad.web.model.message.insights;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.InsightBasicParameters;

public class InsightA2Parameters extends InsightBasicParameters
{
    public InsightA2Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double avgValue)
    {
        super(refDate, deviceType, currentValue, avgValue);
    }

    @Override
    public EnumDynamicRecommendationType getType()
    {
        if (avgValue <= currentValue)
            return EnumDynamicRecommendationType.INSIGHT_A2_DAILY_CONSUMPTION_INCR;
        else 
            return EnumDynamicRecommendationType.INSIGHT_A2_DAILY_CONSUMPTION_DECR;
    }
}