package eu.daiad.web.model.message;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.EnumTimeUnit;
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

    public static class A1Parameters extends BasicParameters
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
    
    public static class A2Parameters extends BasicParameters
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
    
    public static class A3Parameters extends BasicParameters
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
    
    public static class A4Parameters extends BasicParameters
    {
        EnumMap<EnumPartOfDay, Double> partialValues;
       
        public A4Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue)
        {
            // Note: 
            // The current-value is the consumption for the whole day
            // The average-value is not relevant here (equivalent with current-value)
            super(refDate, deviceType, currentValue, currentValue);
            partialValues = new EnumMap<>(EnumPartOfDay.class);
        }
        
        public Double getPart(EnumPartOfDay partOfDay)
        {
            return partialValues.get(partOfDay);
        }
        
        public Double getPartAsPercentage(EnumPartOfDay partOfDay)
        {
            Double partialValue = partialValues.get(partOfDay);
            if (partialValue == null)
                return null;
            return 100.0 * (partialValue / currentValue);
        }
        
        public A4Parameters setPart(EnumPartOfDay partOfDay, double value)
        {
            this.partialValues.put(partOfDay, value);
            return this;
        }
        
        public A4Parameters setParts(Map<EnumPartOfDay, Double> partialValues)
        {
            this.partialValues.putAll(partialValues);
            return this;
        }
        
        @Override
        public EnumDynamicRecommendationType getType()
        {
            EnumDynamicRecommendationType t = null;
            
            Double y1 = partialValues.get(EnumPartOfDay.MORNING); 
            Double y2 = partialValues.get(EnumPartOfDay.AFTERNOON);
            Double y3 = partialValues.get(EnumPartOfDay.NIGHT);
            
            if (y1 == null || y2 == null || y3 == null)
                return EnumDynamicRecommendationType.UNDEFINED;
            
            if (y1 < y2) {
                t = (y2 < y3)? 
                    EnumDynamicRecommendationType.INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT:
                    EnumDynamicRecommendationType.INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON;
            } else {
                // y2 <= y1
                t = (y1 < y3)?
                    EnumDynamicRecommendationType.INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT:
                    EnumDynamicRecommendationType.INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING;
            }
            
            return t;
        }
        
        @Override
        public Map<String, Object> getPairs()
        {
            Map<String, Object> parameters = super.getPairs();
            
            Double p1 = getPartAsPercentage(EnumPartOfDay.MORNING); 
            Double p2 = getPartAsPercentage(EnumPartOfDay.AFTERNOON);
            Double p3 = getPartAsPercentage(EnumPartOfDay.NIGHT);
            
            if (p1 != null)
                parameters.put("morning_percentage", new NumberFormatter(p1, ".#"));
            
            if (p2 != null)
                parameters.put("afternoon_percentage", new NumberFormatter(p2, ".#"));
            
            if (p3 != null)
                parameters.put("night_percentage", new NumberFormatter(p3, ".#"));
            
            return parameters;
        }
    }
    
    /**
     * Note: This is very similar to A2, it only refers to different time units.
     */
    public static class B1Parameters extends BasicParameters
    {
        private final EnumTimeUnit timeUnit;
        
        public B1Parameters(
            DateTime refDate, EnumTimeUnit timeUnit, EnumDeviceType deviceType,
            double currentValue, double avgValue)
        {
            super(refDate, deviceType, currentValue, avgValue);
            
            if (timeUnit != EnumTimeUnit.WEEK && timeUnit != EnumTimeUnit.MONTH)
                throw new IllegalArgumentException(
                    "This insight (B.1) is only relevant to weekly/monthly consumption"
                );
            this.timeUnit = timeUnit;
        }
        
        @Override
        public EnumDynamicRecommendationType getType()
        {
            EnumDynamicRecommendationType t = super.getType();            
            boolean increase = (avgValue < currentValue);
            
            switch (timeUnit) {
            case WEEK:
                t = increase?
                    EnumDynamicRecommendationType.INSIGHT_B1_WEEKLY_CONSUMPTION_INCR:
                    EnumDynamicRecommendationType.INSIGHT_B1_WEEKLY_CONSUMPTION_DECR;
                break;
            case MONTH:
                t = increase?
                    EnumDynamicRecommendationType.INSIGHT_B1_MONTHLY_CONSUMPTION_INCR:
                    EnumDynamicRecommendationType.INSIGHT_B1_MONTHLY_CONSUMPTION_DECR;
                break;
            default:
                // no-op
            }
            return t;
        }
        
        @Override
        public Map<String, Object> getPairs()
        {
            Map<String, Object> parameters = super.getPairs();
            parameters.put("time_unit", timeUnit.name());
            return parameters;
        }
    }
    
    public static class B2Parameters extends BasicParameters
    {
        private final double previousValue;
        private final EnumTimeUnit timeUnit;
        
        public B2Parameters(
            DateTime refDate, EnumTimeUnit timeUnit, EnumDeviceType deviceType, 
            double currentValue, double previousValue)
        {
            super(refDate, deviceType, currentValue, currentValue);
            
            if (timeUnit != EnumTimeUnit.WEEK && timeUnit != EnumTimeUnit.MONTH)
                throw new IllegalArgumentException(
                    "This insight (B.2) is only relevant to weekly/monthly consumption"
                );
            this.timeUnit = timeUnit;
            
            this.previousValue = previousValue;
        }
        
        @Override
        public EnumDynamicRecommendationType getType()
        {
            EnumDynamicRecommendationType t = super.getType();
            boolean increase = (previousValue < currentValue);
            
            switch (timeUnit) {
            case WEEK:
                t = increase?
                    EnumDynamicRecommendationType.INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR:
                    EnumDynamicRecommendationType.INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR;
                break;
            case MONTH:
                t = increase?
                    EnumDynamicRecommendationType.INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR:
                    EnumDynamicRecommendationType.INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR;
                break;
            default:
                // no-op
            }
            return t;
        }
        
        @Override
        public Map<String, Object> getPairs()
        {
            Map<String, Object> parameters = super.getPairs();
            parameters.put("previous_value", Double.valueOf(previousValue));
            parameters.put("previous_consumption", new NumberFormatter(previousValue, ".#"));
            parameters.put("time_unit", timeUnit.name());
            return parameters;
        }
    }
    
    //
    // ~ Constructor
    //
    
    public Insight(EnumDynamicRecommendationType recommendationType, int id)
    {
        super(recommendationType, id);
    }
}