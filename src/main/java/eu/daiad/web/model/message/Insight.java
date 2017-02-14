package eu.daiad.web.model.message;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.EnumDayOfWeek;
import eu.daiad.web.model.EnumPartOfDay;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.NumberFormatter;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.service.ICurrencyRateService;

public class Insight extends Recommendation {

    //
    // ~ Classes for parameters
    //

    public interface ParameterizedTemplate extends Recommendation.ParameterizedTemplate 
    {};
    
    public static class A1Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-3"; 
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double averageValue;
        
        public A1Parameters()
        {
            super();
        }
        
        public A1Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double averageValue)
        {
            super(refDate, deviceType);
            this.averageValue = averageValue;
            this.currentValue = currentValue;
        }
        
        @JsonProperty("currentValue")
        public void setCurrentValue(double y)
        {
            this.currentValue = y;
        }
        
        @JsonProperty("currentValue")
        public Double getCurrentValue()
        {
            return currentValue;
        }

        @JsonProperty("averageValue")
        public void setAverageValue(double y)
        {
            this.averageValue = y;
        }
        
        @JsonProperty("averageValue")
        public Double getAverageValue()
        {
            return averageValue;
        }

        @JsonIgnore
        public EnumDayOfWeek getDayOfWeek()
        {
            return EnumDayOfWeek.valueOf(refDate.getDayOfWeek());   
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            if (averageValue <= currentValue)
                return EnumRecommendationTemplate.INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR;
            else 
                return EnumRecommendationTemplate.INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR;
        }
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("value", currentValue);
            parameters.put("consumption", currentValue);     
            
            parameters.put("average_value", averageValue);
            parameters.put("average_consumption", averageValue);
            
            Double percentChange = 100.0 * Math.abs(((currentValue - averageValue) / averageValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));
          
            parameters.put("day", refDate.toDate());
            parameters.put("day_of_week", getDayOfWeek());
            
            return parameters;
        }
        
        @Override
        public A1Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    public static class A2Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-3"; 
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double averageValue;
        
        public A2Parameters()
        {
            super();
        }
        
        public A2Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double averageValue)
        {
            super(refDate, deviceType);
            this.averageValue = averageValue;
            this.currentValue = currentValue;
        }

        @JsonProperty("currentValue")
        public void setCurrentValue(double y)
        {
            this.currentValue = y;
        }
        
        @JsonProperty("currentValue")
        public Double getCurrentValue()
        {
            return currentValue;
        }

        @JsonProperty("averageValue")
        public void setAverageValue(double y)
        {
            this.averageValue = y;
        }
        
        @JsonProperty("averageValue")
        public Double getAverageValue()
        {
            return averageValue;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("value", currentValue);
            parameters.put("consumption", currentValue);     
            
            parameters.put("average_value", averageValue);
            parameters.put("average_consumption", averageValue);
            
            Double percentChange = 100.0 * Math.abs(((currentValue - averageValue) / averageValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));
            
            return parameters;
        }
        
        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            if (averageValue <= currentValue)
                return EnumRecommendationTemplate.INSIGHT_A2_DAILY_CONSUMPTION_INCR;
            else 
                return EnumRecommendationTemplate.INSIGHT_A2_DAILY_CONSUMPTION_DECR;
        }
        
        @Override
        public A2Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    public static class A3Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-3"; 
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double averageValue;
        
        @NotNull
        private EnumPartOfDay partOfDay;
        
        public A3Parameters()
        {
            super();
        }
        
        public A3Parameters(
            DateTime refDate, EnumPartOfDay partOfDay, EnumDeviceType deviceType,
            double currentValue, double averageValue)
        {
            super(refDate, deviceType);
            this.averageValue = averageValue;
            this.currentValue = currentValue;
            this.partOfDay = partOfDay;
        }

        @JsonProperty("currentValue")
        public void setCurrentValue(double y)
        {
            this.currentValue = y;
        }
        
        @JsonProperty("currentValue")
        public Double getCurrentValue()
        {
            return currentValue;
        }

        @JsonProperty("averageValue")
        public void setAverageValue(double y)
        {
            this.averageValue = y;
        }
        
        @JsonProperty("averageValue")
        public Double getAverageValue()
        {
            return averageValue;
        }
        
        @JsonProperty("partOfDay")
        public EnumPartOfDay getPartOfDay()
        {
            return partOfDay;
        }
        
        @JsonProperty("partOfDay")
        public void setPartOfDay(EnumPartOfDay partOfDay)
        {
            this.partOfDay = partOfDay;
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;
            switch (partOfDay) {
            case MORNING:
                t = (averageValue <= currentValue)?
                        EnumRecommendationTemplate.INSIGHT_A3_MORNING_CONSUMPTION_INCR:
                        EnumRecommendationTemplate.INSIGHT_A3_MORNING_CONSUMPTION_DECR;
                break;
            case AFTERNOON:
                t = (averageValue <= currentValue)?
                        EnumRecommendationTemplate.INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR:
                        EnumRecommendationTemplate.INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR;
                break;
            case NIGHT:
                t = (averageValue <= currentValue)?
                        EnumRecommendationTemplate.INSIGHT_A3_NIGHT_CONSUMPTION_INCR:
                        EnumRecommendationTemplate.INSIGHT_A3_NIGHT_CONSUMPTION_DECR;
                break;
            }  
            return t;
        }
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();  
            
            parameters.put("value", currentValue);
            parameters.put("consumption", currentValue);     
            
            parameters.put("average_value", averageValue);
            parameters.put("average_consumption", averageValue);
            
            Double percentChange = 100.0 * Math.abs(((currentValue - averageValue) / averageValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));
            
            parameters.put("part_of_day", partOfDay);
            
            return parameters;
        }
        
        @Override
        public A3Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    public static class A4Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-3"; 
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double totalValue;
        
        @NotNull
        private EnumMap<EnumPartOfDay, Double> parts = new EnumMap<>(EnumPartOfDay.class);
       
        public A4Parameters()
        {
            super();
        }
        
        public A4Parameters(
            DateTime refDate, EnumDeviceType deviceType, double totalValue)
        {
            super(refDate, deviceType);
            this.totalValue = totalValue;
        }
        
        @JsonProperty("totalValue")
        public void setTotalValue(double y)
        {
            this.totalValue = y;
        }
        
        @JsonProperty("totalValue")
        public Double getTotalValue()
        {
            return totalValue;
        }
        
        @JsonProperty("parts")
        public void setParts(Map<EnumPartOfDay, Double> partialValues)
        {
            for (Map.Entry<EnumPartOfDay, Double> e: partialValues.entrySet()) {
                EnumPartOfDay p = e.getKey();
                Double y = e.getValue();
                if (y != null)
                    this.parts.put(p, y);
            }
        }
        
        @JsonProperty("parts")
        public Map<EnumPartOfDay, Double> getParts()
        {            
            return parts;
        }
        
        @Size(max = 0)
        public Set<EnumPartOfDay> getMissingParts()
        {
            return EnumSet.complementOf(EnumSet.copyOf(parts.keySet()));
        }
        
        public Double getPart(EnumPartOfDay partOfDay)
        {
            return parts.get(partOfDay);
        }
        
        public A4Parameters withPart(EnumPartOfDay partOfDay, double value)
        {
            this.parts.put(partOfDay, value);
            return this;
        }
        
        public A4Parameters withParts(Map<EnumPartOfDay, Double> vals)
        {
            setParts(vals);
            return this;
        }
        
        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;
            
            Double y1 = parts.get(EnumPartOfDay.MORNING); 
            Double y2 = parts.get(EnumPartOfDay.AFTERNOON);
            Double y3 = parts.get(EnumPartOfDay.NIGHT);
            
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
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("value", totalValue);
            parameters.put("consumption", totalValue); 
            
            Double y1 = parts.get(EnumPartOfDay.MORNING); 
            Double y2 = parts.get(EnumPartOfDay.AFTERNOON);
            Double y3 = parts.get(EnumPartOfDay.NIGHT);
            
            if (y1 == null || y2 == null || y3 == null)
                return parameters;
            
            Double p1 = 100.0 * (y1 / totalValue);
            Double p2 = 100.0 * (y2 / totalValue);
            Double p3 = 100.0 * (y3 / totalValue);
            
            parameters.put("morning_consumption", y1);
            parameters.put("morning_percentage", Integer.valueOf(p1.intValue()));
            
            parameters.put("afternoon_consumption", y2);
            parameters.put("afternoon_percentage", Integer.valueOf(p2.intValue()));
            
            parameters.put("night_consumption", y3);
            parameters.put("night_percentage", Integer.valueOf(p3.intValue()));
            
            return parameters;
        }
        
        @Override
        public A4Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    public static class B1Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for weekly volume consumption */
        private static final String MIN_VALUE = "1E+0"; 
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double averageValue;
        
        @NotNull
        private EnumTimeUnit timeUnit;
        
        public B1Parameters()
        {
            super();
        }
        
        public B1Parameters(
            DateTime refDate, EnumTimeUnit timeUnit, EnumDeviceType deviceType,
            double currentValue, double averageValue)
        {
            super(refDate, deviceType);
            this.averageValue = averageValue;
            this.currentValue = currentValue;
            this.timeUnit = timeUnit;
        }
        
        @JsonProperty("currentValue")
        public void setCurrentValue(double y)
        {
            this.currentValue = y;
        }
        
        @JsonProperty("currentValue")
        public Double getCurrentValue()
        {
            return currentValue;
        }

        @JsonProperty("averageValue")
        public void setAverageValue(double y)
        {
            this.averageValue = y;
        }
        
        @JsonProperty("averageValue")
        public Double getAverageValue()
        {
            return averageValue;
        }
        
        @JsonProperty("timeUnit")
        public EnumTimeUnit getTimeUnit()
        {
            return timeUnit;
        }
        
        @JsonProperty("timeUnit")
        public void setTimeUnit(EnumTimeUnit timeUnit)
        {
            this.timeUnit = timeUnit;
        }
        
        @JsonIgnore
        @AssertTrue(message = "This insight (B.1) is only relevant to weekly/monthly consumption")
        public boolean hasProperTimeUnit()
        {
            return (timeUnit == EnumTimeUnit.WEEK || timeUnit == EnumTimeUnit.MONTH);
        }
        
        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;            
            boolean increase = (averageValue < currentValue);
            
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
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("value", currentValue);
            parameters.put("consumption", currentValue);     
            
            parameters.put("average_value", averageValue);
            parameters.put("average_consumption", averageValue);
            
            Double percentChange = 100.0 * Math.abs(((currentValue - averageValue) / averageValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));
            
            parameters.put("time_unit", timeUnit.name());
            
            return parameters;
        }
        
        @Override
        public B1Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    public static class B2Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for weekly volume consumption */
        private static final String MIN_VALUE = "1E+0"; 
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double previousValue;
        
        @NotNull
        private EnumTimeUnit timeUnit;
        
        public B2Parameters()
        {
            super();
        }
        
        public B2Parameters(
            DateTime refDate, EnumTimeUnit timeUnit, EnumDeviceType deviceType, 
            double currentValue, double previousValue)
        {
            super(refDate, deviceType);
            this.currentValue = currentValue;
            this.previousValue = previousValue;
            this.timeUnit = timeUnit;
        }
        
        @JsonProperty("currentValue")
        public void setCurrentValue(double y)
        {
            this.currentValue = y;
        }
        
        @JsonProperty("currentValue")
        public Double getCurrentValue()
        {
            return currentValue;
        }
        
        @JsonProperty("previousValue")
        public void setPreviousValue(double y)
        {
            this.previousValue = y;
        }
        
        @JsonProperty("previousValue")
        public Double getPreviousValue()
        {
            return previousValue;
        }
        
        @JsonProperty("timeUnit")
        public EnumTimeUnit getTimeUnit()
        {
            return timeUnit;
        }
        
        @JsonProperty("timeUnit")
        public void setTimeUnit(EnumTimeUnit timeUnit)
        {
            this.timeUnit = timeUnit;
        }
        
        @JsonIgnore
        @AssertTrue(message = "This insight (B.2) is only relevant to weekly/monthly consumption")
        public boolean hasProperTimeUnit()
        {
            return (timeUnit == EnumTimeUnit.WEEK || timeUnit == EnumTimeUnit.MONTH);
        }
        
        @JsonIgnore
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
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("value", currentValue);
            parameters.put("consumption", currentValue);     
            
            parameters.put("previous_value", previousValue);
            parameters.put("previous_consumption", previousValue);
            
            Double percentChange = 100.0 * Math.abs(((currentValue - previousValue) / previousValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));
            
            parameters.put("time_unit", timeUnit.name());
            
            return parameters;
        }
        
        @Override
        public B2Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    public static class B3Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {        
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-3"; 
        
        /** The average daily consumption for the particular day-of-week */
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;
        
        /** The average daily consumption for all week days */
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double averageValue;
        
        /** The day of week for this consumption peak (high or low) */
        @NotNull
        private EnumDayOfWeek dayOfWeek; 
        
        public B3Parameters()
        {
            super();
        }
        
        public B3Parameters(
            DateTime refDate, EnumDeviceType deviceType, 
            double currentValue, double averageValue, EnumDayOfWeek dayOfWeek)
        {
            super(refDate, deviceType);
            this.averageValue = averageValue;
            this.currentValue = currentValue;
            this.dayOfWeek = dayOfWeek;
        }
        
        @JsonProperty("currentValue")
        public void setCurrentValue(double y)
        {
            this.currentValue = y;
        }
        
        @JsonProperty("currentValue")
        public Double getCurrentValue()
        {
            return currentValue;
        }

        @JsonProperty("averageValue")
        public void setAverageValue(double y)
        {
            this.averageValue = y;
        }
        
        @JsonProperty("averageValue")
        public Double getAverageValue()
        {
            return averageValue;
        }
        
        @JsonProperty("dayOfWeek")
        public EnumDayOfWeek getDayOfWeek()
        {
            return dayOfWeek;
        }
        
        @JsonProperty("dayOfWeek")
        public void setDayOfWeek(EnumDayOfWeek day)
        {
            this.dayOfWeek = day;
        }
        
        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return (currentValue < averageValue)?
                EnumRecommendationTemplate.INSIGHT_B3_DAYOFWEEK_CONSUMPTION_LOW:
                EnumRecommendationTemplate.INSIGHT_B3_DAYOFWEEK_CONSUMPTION_PEAK;
        }
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("value", currentValue);
            parameters.put("consumption", currentValue);     
            
            parameters.put("average_value", averageValue);
            parameters.put("average_consumption", averageValue);
            
            Double percentChange = 100.0 * Math.abs(((currentValue - averageValue) / averageValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));
            
            int dow = dayOfWeek.toInteger();
            parameters.put("day", refDate.withDayOfWeek(dow).toDate());
            parameters.put("day_of_week", dayOfWeek);
            
            return parameters;
        }
        
        @Override
        public B3Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    public static class B4Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-3"; 
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double weekdayValue;        
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double weekendValue;
        
        public B4Parameters()
        {
            super();
        }
        
        public B4Parameters(
            DateTime refDate, EnumDeviceType deviceType, double weekdayValue, double weekendValue)
        {
            super(refDate, deviceType);
            this.weekdayValue = weekdayValue;
            this.weekendValue = weekendValue;
        }
        
        @JsonProperty("weekdayValue")
        public void setWeekdayValue(double y)
        {
            this.weekdayValue = y;
        }
        
        @JsonProperty("weekdayValue")
        public Double getWeekdayValue()
        {
            return weekdayValue;
        }
        
        @JsonProperty("weekendValue")
        public void setWeekendValue(double y)
        {
            this.weekendValue = y;
        }
        
        @JsonProperty("weekendValue")
        public Double getWeekendValue()
        {
            return weekendValue;
        }
        
        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return (weekdayValue < weekendValue)?
                EnumRecommendationTemplate.INSIGHT_B4_MORE_ON_WEEKEND:
                EnumRecommendationTemplate.INSIGHT_B4_LESS_ON_WEEKEND;    
        }
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();

            parameters.put("weekday_consumption", weekdayValue);
            
            parameters.put("weekend_consumption", weekendValue);
            
            Double percentChange = 100.0 * Math.abs((weekendValue - weekdayValue) / weekdayValue);
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));
            
            return parameters;
        }
        
        @Override
        public B4Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    public static class B5Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for monthly volume consumption */
        private static final String MIN_VALUE = "1E+1"; 
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;
        
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double previousValue;
        
        public B5Parameters()
        {}
        
        public B5Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double previousValue)
        {
            super(refDate, deviceType);
            this.currentValue = currentValue;
            this.previousValue = previousValue;
        }
        
        @JsonProperty("currentValue")
        public void setCurrentValue(double y)
        {
            this.currentValue = y;
        }
        
        @JsonProperty("currentValue")
        public Double getCurrentValue()
        {
            return currentValue;
        }
        
        @JsonProperty("previousValue")
        public void setPreviousValue(double y)
        {
            this.previousValue = y;
        }
        
        @JsonProperty("previousValue")
        public Double getPreviousValue()
        {
            return previousValue;
        }
        
        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return (previousValue < currentValue)?
                EnumRecommendationTemplate.INSIGHT_B5_MONTHLY_CONSUMPTION_INCR:
                EnumRecommendationTemplate.INSIGHT_B5_MONTHLY_CONSUMPTION_DECR;    
        }
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("value", currentValue);
            parameters.put("consumption", currentValue);     
            
            parameters.put("previous_value", previousValue);
            parameters.put("previous_consumption", previousValue);
            
            Double percentChange = 100.0 * Math.abs(((currentValue - previousValue) / previousValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));
            
            return parameters;
        }
        
        @Override
        public B5Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
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