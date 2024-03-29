package eu.daiad.common.service.message.resolvers;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.common.annotate.message.MessageGenerator;
import eu.daiad.common.model.EnumDayOfWeek;
import eu.daiad.common.model.EnumTimeAggregation;
import eu.daiad.common.model.EnumTimeUnit;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.message.Alert.ParameterizedTemplate;
import eu.daiad.common.model.message.EnumAlertTemplate;
import eu.daiad.common.model.message.Message;
import eu.daiad.common.model.message.MessageResolutionStatus;
import eu.daiad.common.model.message.SimpleMessageResolutionStatus;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryBuilder;
import eu.daiad.common.model.query.DataQueryResponse;
import eu.daiad.common.model.query.EnumDataField;
import eu.daiad.common.model.query.EnumMeasurementDataSource;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.Point;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.service.ICurrencyRateService;
import eu.daiad.common.service.IDataService;
import eu.daiad.common.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY)
@Component
@Scope("prototype")
public class AlertWeeklyWaterBudget extends AbstractAlertResolver
{
    public static final int BUDGET_NEAR_PERCENTAGE_THRESHOLD = 80; // must be <= 100

    public static final int BUDGET_OVER_PERCENTAGE_THRESHOLD = 110; // must be > 100

    @Autowired
    IDataService dataService;

    public static abstract class BasicParameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+0")
        private Double value;

        @NotNull
        @DecimalMin("5E+0")
        private Double budget;

        @NotNull
        @DecimalMin("0")
        @DecimalMax("200")
        private Integer percentThreshold;

        public BasicParameters()
        {}

        protected BasicParameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double budget, int percentThreshold)
        {
            super(refDate, deviceType);
            this.value = value;
            this.budget = budget;
            this.percentThreshold = percentThreshold;
        }

        @JsonProperty("value")
        public Double getValue()
        {
            return value;
        }

        @JsonProperty("value")
        public void setValue(double value)
        {
            this.value = value;
        }

        @JsonProperty("budget")
        public Double getBudget()
        {
            return budget;
        }

        @JsonProperty("budget")
        public void setBudget(double budget)
        {
            this.budget = budget;
        }

        @JsonProperty("percentThreshold")
        public int getPercentThreshold()
        {
            return percentThreshold;
        }

        @JsonProperty("percentThreshold")
        public void setPercentThreshold(int percentThreshold)
        {
            this.percentThreshold = percentThreshold;
        }

        @Override
        public ParameterizedTemplate withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }

        @Override
        @JsonIgnore
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();

            parameters.put("value", value);
            parameters.put("consumption", value);

            parameters.put("budget", budget);
            parameters.put("remaining", Double.valueOf((budget > value)? (budget - value) : .0));

            parameters.put("percent_threshold", percentThreshold);

            Double percentUsed = 100.0 * (value / budget);
            parameters.put("percent_consumed", Integer.valueOf(percentUsed.intValue()));

            return parameters;
        }
    }

    public static class NearBudgetParameters extends BasicParameters
    {
        public NearBudgetParameters()
        {
            super();
        }

        public NearBudgetParameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double budget, int percentThreshold)
        {
            super(refDate, deviceType, value, budget, percentThreshold);
        }

        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return (deviceType == EnumDeviceType.AMPHIRO)?
                EnumAlertTemplate.NEAR_WEEKLY_SHOWER_BUDGET: EnumAlertTemplate.NEAR_WEEKLY_WATER_BUDGET;
        }
    }

    public static class ExceededBudgetParameters extends BasicParameters
    {
        public ExceededBudgetParameters()
        {
            super();
        }

        public ExceededBudgetParameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double budget, int percentThreshold)
        {
            super(refDate, deviceType, value, budget, percentThreshold);
        }

        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return (deviceType == EnumDeviceType.AMPHIRO)?
                EnumAlertTemplate.REACHED_WEEKLY_SHOWER_BUDGET: EnumAlertTemplate.REACHED_WEEKLY_WATER_BUDGET;
        }
    }

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        DateTime start = refDate.minusWeeks(1)
            .withDayOfWeek(DateTimeConstants.MONDAY)
            .withTimeAtStartOfDay();

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .sliding(start, +1, EnumTimeUnit.WEEK, EnumTimeAggregation.WEEK)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(deviceType);

        Interval interval = query.getTime().asInterval();
        Double consumption  = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval)):
            null;
        if (consumption == null)
            return Collections.emptyList();

        int budget = config.getBudget(deviceType, EnumTimeUnit.WEEK);
        double percentConsumed = 100.0 * (consumption / budget);

        ParameterizedTemplate parameterizedTemplate;
        if (percentConsumed < BUDGET_NEAR_PERCENTAGE_THRESHOLD) {
            parameterizedTemplate = null;
        } else if (percentConsumed < BUDGET_OVER_PERCENTAGE_THRESHOLD) {
            parameterizedTemplate = new NearBudgetParameters(
                refDate, deviceType, consumption, budget,  BUDGET_NEAR_PERCENTAGE_THRESHOLD);
        } else {
            parameterizedTemplate = new ExceededBudgetParameters(
                refDate, deviceType, consumption, budget,  BUDGET_OVER_PERCENTAGE_THRESHOLD);
        }
        if (parameterizedTemplate == null)
            return Collections.emptyList();

        MessageResolutionStatus<ParameterizedTemplate> result =
            new SimpleMessageResolutionStatus<>(parameterizedTemplate);
        return Collections.singletonList(result);
    }

}
