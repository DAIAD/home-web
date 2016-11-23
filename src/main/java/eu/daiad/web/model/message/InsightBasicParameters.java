package eu.daiad.web.model.message;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;

public abstract class InsightBasicParameters extends AbstractMessageParameters
{
    // The reference value. Note that precise semantics 
    // are insight-specific (e.g. for A.1 is value on the reference date)
    protected final double currentValue;
    
    // The average of past values. Note that precise semantics 
    // are insight-specific (e.g. for A.1 is last P values on a particular week day)
    protected final double avgValue;
    
    public InsightBasicParameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double avgValue)
    {
        super(refDate, deviceType);
        this.avgValue = avgValue;
        this.currentValue = currentValue;
    }
    
    public double getCurrentValue()
    {
        return currentValue;
    }

    public double getAvgValue()
    {
        return avgValue;
    }
    
    public double getPercentChange()
    {
        return ((currentValue - avgValue) / avgValue) * 100.0;
    }
    
    @Override
    public Map<String, Object> asParameters()
    {
        Map<String, Object> parameters = super.asParameters();
        
        parameters.put("value", Double.valueOf(currentValue));
        parameters.put("average_value", Double.valueOf(avgValue));
        
        parameters.put("consumption", new NumberFormatter(currentValue, ".#"));
        parameters.put("average_consumption", new NumberFormatter(avgValue, ".#"));
        
        double percentChange = getPercentChange();
        parameters.put("percent_change", Integer.valueOf((int) Math.abs(percentChange)));
        
        return parameters;
    }
}
