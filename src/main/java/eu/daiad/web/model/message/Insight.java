package eu.daiad.web.model.message;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.NumberFormatter;
import eu.daiad.web.model.device.EnumDeviceType;

public class Insight extends DynamicRecommendation {

    //
    // ~ Classes for parameters
    //

    public abstract static class BasicParameters extends DynamicRecommendation.AbstractParameters
    {
        // The reference value. Note that precise semantics 
        // are insight-specific (e.g. for A.1 is value on the reference date)
        protected final double currentValue;

        // The average of past values. Note that precise semantics 
        // are insight-specific (e.g. for A.1 is last P values on a particular week day)
        protected final double avgValue;

        public BasicParameters(
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
        public Map<String, Object> getPairs()
        {
            Map<String, Object> parameters = super.getPairs();

            parameters.put("value", Double.valueOf(currentValue));
            parameters.put("average_value", Double.valueOf(avgValue));

            parameters.put("consumption", new NumberFormatter(currentValue, ".#"));
            parameters.put("average_consumption", new NumberFormatter(avgValue, ".#"));

            double percentChange = getPercentChange();
            parameters.put("percent_change", Integer.valueOf((int) Math.abs(percentChange)));

            return parameters;
        }
    }

    protected static class A1Parameters extends BasicParameters
    {
        public A1Parameters(
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
        public Map<String, Object> getPairs()
        {
            Map<String, Object> parameters = super.getPairs();
            parameters.put("day_of_week", Integer.valueOf(getDayOfWeek()));
            return parameters;
        }
    }
    
    protected static class A2Parameters extends BasicParameters
    {
        public A2Parameters(
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
    
    protected static class A3Parameters extends BasicParameters
    {
        protected final EnumPartOfDay partOfDay;
        
        public A3Parameters(
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
        public Map<String, Object> getPairs()
        {
            Map<String, Object> parameters = super.getPairs();  
            parameters.put("part_of_day", partOfDay);
            return parameters;
        }
    }
        
    //
    // ~ Factories providing several subclasses of parameters
    //
    
    public static BasicParameters newA1Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double avgValue)
    {
        return new A1Parameters(refDate, deviceType, currentValue, avgValue);
    }
    
    public static BasicParameters newA2Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double avgValue)
    {
        return new A2Parameters(refDate, deviceType, currentValue, avgValue);
    }
    
    public static BasicParameters newA3Parameters(
            DateTime refDate, EnumPartOfDay partOfDay, EnumDeviceType deviceType,
            double currentValue, double avgValue)
    {
        return new A3Parameters(refDate, partOfDay, deviceType, currentValue, avgValue);
    }
    
    //
    // ~ Constructor
    //
    
    public Insight(EnumDynamicRecommendationType recommendationType, int id)
    {
        super(recommendationType, id);
    }
}