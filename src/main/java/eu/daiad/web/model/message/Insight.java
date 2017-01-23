package eu.daiad.web.model.message;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.util.Assert;

import eu.daiad.web.model.EnumDayOfWeek;
import eu.daiad.web.model.EnumPartOfDay;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.NumberFormatter;
import eu.daiad.web.model.device.EnumDeviceType;

public class Insight extends Recommendation {

    //
    // ~ Classes for parameters
    //

    public interface ParameterizedTemplate extends Recommendation.ParameterizedTemplate 
    {};
    
    protected abstract static class BasicParameters extends Recommendation.AbstractParameterizedTemplate
        implements ParameterizedTemplate
    {
        // The reference value. Note that precise semantics 
        // are insight-specific (e.g. for A.1 is value on the reference date)
        protected final Double currentValue;

        // The average of past values. Note that precise semantics 
        // are insight-specific (e.g. for A.1 is last P values on a particular week day)
        protected final Double avgValue;

        public BasicParameters(
            DateTime refDate, EnumDeviceType deviceType, Double currentValue, Double avgValue)
        {
            super(refDate, deviceType);
            this.avgValue = avgValue;
            this.currentValue = currentValue;
        }

        public Double getCurrentValue()
        {
            return currentValue;
        }

        public Double getAverageValue()
        {
            return avgValue;
        }

        public Double getPercentChange()
        {
            return (currentValue == null || avgValue == null)? 
                null: ((currentValue - avgValue) / avgValue) * 100.0;
        }

        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            if (currentValue != null) {
                parameters.put("value", currentValue);
                parameters.put("consumption", new NumberFormatter(currentValue, ".#"));
            }
            
            if (avgValue != null) {
                parameters.put("average_value", avgValue);
                parameters.put("average_consumption", new NumberFormatter(avgValue, ".#"));
            }
                
            Double percentChange = getPercentChange();
            if (percentChange != null)
                parameters.put("percent_change", 
                    Integer.valueOf((int) Math.abs(percentChange)));

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
        
        public EnumDayOfWeek getDayOfWeek()
        {
            return EnumDayOfWeek.valueOf(refDate.getDayOfWeek());
        }

        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            if (avgValue <= currentValue)
                return EnumRecommendationTemplate.INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR;
            else 
                return EnumRecommendationTemplate.INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR;
        }
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            parameters.put("day_of_week", refDate.getDayOfWeek());
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
        public EnumRecommendationTemplate getTemplate()
        {
            if (avgValue <= currentValue)
                return EnumRecommendationTemplate.INSIGHT_A2_DAILY_CONSUMPTION_INCR;
            else 
                return EnumRecommendationTemplate.INSIGHT_A2_DAILY_CONSUMPTION_DECR;
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
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;
            switch (partOfDay) {
            case MORNING:
                t = (avgValue <= currentValue)?
                        EnumRecommendationTemplate.INSIGHT_A3_MORNING_CONSUMPTION_INCR:
                        EnumRecommendationTemplate.INSIGHT_A3_MORNING_CONSUMPTION_DECR;
                break;
            case AFTERNOON:
                t = (avgValue <= currentValue)?
                        EnumRecommendationTemplate.INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR:
                        EnumRecommendationTemplate.INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR;
                break;
            case NIGHT:
                t = (avgValue <= currentValue)?
                        EnumRecommendationTemplate.INSIGHT_A3_NIGHT_CONSUMPTION_INCR:
                        EnumRecommendationTemplate.INSIGHT_A3_NIGHT_CONSUMPTION_DECR;
                break;
            }  
            return t;
        }
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();  
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
            // The average-value is not relevant here
            super(refDate, deviceType, currentValue, null);
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
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;
            
            Double y1 = partialValues.get(EnumPartOfDay.MORNING); 
            Double y2 = partialValues.get(EnumPartOfDay.AFTERNOON);
            Double y3 = partialValues.get(EnumPartOfDay.NIGHT);
            
            if (y1 == null || y2 == null || y3 == null)
                return null;
            
            if (y1 < y2) {
                t = (y2 < y3)? 
                    EnumRecommendationTemplate.INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT:
                    EnumRecommendationTemplate.INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON;
            } else {
                // y2 <= y1
                t = (y1 < y3)?
                    EnumRecommendationTemplate.INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT:
                    EnumRecommendationTemplate.INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING;
            }
            
            return t;
        }
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
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
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;            
            boolean increase = (avgValue < currentValue);
            
            switch (timeUnit) {
            case WEEK:
                t = increase?
                    EnumRecommendationTemplate.INSIGHT_B1_WEEKLY_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B1_WEEKLY_CONSUMPTION_DECR;
                break;
            case MONTH:
                t = increase?
                    EnumRecommendationTemplate.INSIGHT_B1_MONTHLY_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B1_MONTHLY_CONSUMPTION_DECR;
                break;
            default:
                // no-op
            }
            return t;
        }
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
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
            super(refDate, deviceType, currentValue, null);
            
            if (timeUnit != EnumTimeUnit.WEEK && timeUnit != EnumTimeUnit.MONTH)
                throw new IllegalArgumentException(
                    "This insight (B.2) is only relevant to weekly/monthly consumption"
                );
            this.timeUnit = timeUnit;
            
            this.previousValue = previousValue;
        }
        
        public Double getPercentChange()
        {
            return ((currentValue - previousValue) / previousValue) * 100.0;
        }
        
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;
            boolean increase = (previousValue < currentValue);
            
            switch (timeUnit) {
            case WEEK:
                t = increase?
                    EnumRecommendationTemplate.INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR;
                break;
            case MONTH:
                t = increase?
                    EnumRecommendationTemplate.INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR;
                break;
            default:
                // no-op
            }
            return t;
        }
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("previous_value", Double.valueOf(previousValue));
            parameters.put("previous_consumption", new NumberFormatter(previousValue, ".#"));
            
            parameters.put("time_unit", timeUnit.name());
            
            return parameters;
        }
    }
    
    public static class B3Parameters extends BasicParameters
    {        
        private final EnumDayOfWeek dayOfWeek; 
        
        /**
         * @param refDate
         * @param deviceType
         * @param currentValue The average consumption for the particular day-of-week
         * @param avgValue The average consumption for all week days
         * @param dayOfWeek The day of week for this consumption peak (high or low)
         */
        public B3Parameters(
            DateTime refDate, EnumDeviceType deviceType, 
            double currentValue, double avgValue, EnumDayOfWeek dayOfWeek)
        {
            super(refDate, deviceType, currentValue, avgValue);
            this.dayOfWeek = dayOfWeek;
        }
        
        public EnumDayOfWeek getDayOfWeek()
        {
            return dayOfWeek;
        }
        
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return (currentValue < avgValue)?
                EnumRecommendationTemplate.INSIGHT_B3_DAYOFWEEK_CONSUMPTION_LOW:
                EnumRecommendationTemplate.INSIGHT_B3_DAYOFWEEK_CONSUMPTION_PEAK;
        }
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            parameters.put("day_of_week", dayOfWeek.toInteger());
            return parameters;
        }
    }
    
    public static class B4Parameters extends BasicParameters
    {
        private final double weekdayValue;        
        private final double weekendValue;
        
        public B4Parameters(
            DateTime refDate, EnumDeviceType deviceType, double weekdayValue, double weekendValue)
        {
            super(refDate, deviceType, null, null);
            this.weekdayValue = weekdayValue;
            this.weekendValue = weekendValue;
        }
        
        public Double getPercentChange()
        {
            return ((weekendValue - weekdayValue) / weekdayValue) * 100.0;
        }
        
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return (weekdayValue < weekendValue)?
                EnumRecommendationTemplate.INSIGHT_B4_MORE_ON_WEEKEND:
                EnumRecommendationTemplate.INSIGHT_B4_LESS_ON_WEEKEND;    
        }
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("weekday_value", Double.valueOf(weekdayValue));
            parameters.put("weekday_consumption", new NumberFormatter(weekdayValue, ".#"));
            
            parameters.put("weekend_value", Double.valueOf(weekendValue));
            parameters.put("weekend_consumption", new NumberFormatter(weekendValue, ".#"));
            
            return parameters;
        }
    }
    
    public static class B5Parameters extends BasicParameters
    {
        private final double previousValue;
        
        public B5Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double previousValue)
        {
            super(refDate, deviceType, currentValue, null);
            this.previousValue = previousValue;
        }
        
        public Double getPercentChange()
        {
            return ((currentValue - previousValue) / previousValue) * 100.0;
        }
        
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return (previousValue < currentValue)?
                EnumRecommendationTemplate.INSIGHT_B5_MONTHLY_CONSUMPTION_INCR:
                EnumRecommendationTemplate.INSIGHT_B5_MONTHLY_CONSUMPTION_DECR;    
        }
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("previous_value", Double.valueOf(previousValue));
            parameters.put("previous_consumption", new NumberFormatter(previousValue, ".#"));
            
            return parameters;
        }
    }
    
    //
    // ~ Constructor
    //
    
    public Insight(EnumRecommendationTemplate recommendationTemplate, int id)
    {
        super(id, recommendationTemplate);
    }
    
    public Insight(EnumRecommendationType recommendationType, int id)
    {
        super(id, recommendationType);
    }
}