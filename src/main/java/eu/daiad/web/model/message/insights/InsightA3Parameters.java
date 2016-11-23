package eu.daiad.web.model.message.insights;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.EnumPartOfDay;
import eu.daiad.web.model.message.InsightBasicParameters;

public class InsightA3Parameters extends InsightBasicParameters
{
    protected final EnumPartOfDay partOfDay;
    
    public InsightA3Parameters(
            DateTime refDate, EnumPartOfDay partOfDay, EnumDeviceType deviceType,
            double currentValue, double avgValue)
    {
        super(refDate, deviceType, currentValue, avgValue);
        this.partOfDay = partOfDay;
    }

    public EnumPartOfDay getPartOfDay()
    {
        return partOfDay;
    }

    @Override
    public EnumDynamicRecommendationType getType()
    {
        EnumDynamicRecommendationType t = null;
        switch (partOfDay) {
        case MORNING:
            t = (avgValue <= currentValue)?
                    EnumDynamicRecommendationType.INSIGHT_A3_MORNING_CONSUMPTION_INCR:
                    EnumDynamicRecommendationType.INSIGHT_A3_MORNING_CONSUMPTION_DECR;
            break;
        case AFTERNOON:
            t = (avgValue <= currentValue)?
                    EnumDynamicRecommendationType.INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR:
                    EnumDynamicRecommendationType.INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR;
            break;
        case NIGHT:
            t = (avgValue <= currentValue)?
                    EnumDynamicRecommendationType.INSIGHT_A3_NIGHT_CONSUMPTION_INCR:
                    EnumDynamicRecommendationType.INSIGHT_A3_NIGHT_CONSUMPTION_DECR;
            break;
        }
        
        return t;
    }
    
    @Override
    public Map<String, Object> asParameters()
    {
        Map<String, Object> parameters = super.asParameters();
        
        parameters.put("part_of_day", partOfDay);
        return parameters;
    }
}
