package eu.daiad.common.service.message;

import static eu.daiad.common.model.device.EnumDeviceType.AMPHIRO;
import static eu.daiad.common.model.device.EnumDeviceType.METER;

import java.util.EnumMap;
import java.util.Map;

import org.joda.time.DateTimeConstants;
import org.joda.time.Period;

import eu.daiad.common.model.EnumDayOfWeek;
import eu.daiad.common.model.EnumTimeUnit;
import eu.daiad.common.model.device.EnumDeviceType;

public class Configuration
{
    public static final int BUDGET_METER_DAILY_LIMIT = 300;

    public static final int BUDGET_METER_WEEKLY_LIMIT = 2100;

    public static final int BUDGET_METER_MONTHLY_LIMIT = 9000;

    public static final int BUDGET_AMPHIRO_DAILY_LIMIT = 100;

    public static final int BUDGET_AMPHIRO_WEEKLY_LIMIT = 700;

    public static final int BUDGET_AMPHIRO_MONTHLY_LIMIT = 3000;

    public static final double VOLUME_METER_DAILY_THRESHOLD = 7.0;

    public static final double VOLUME_AMPHIRO_DAILY_THRESHOLD = 5.0;

    public static final double VOLUME_METER_WEEKLY_THRESHOLD =
        DateTimeConstants.DAYS_PER_WEEK * VOLUME_METER_DAILY_THRESHOLD;

    public static final double VOLUME_AMPHIRO_WEEKLY_THRESHOLD =
        DateTimeConstants.DAYS_PER_WEEK * VOLUME_AMPHIRO_DAILY_THRESHOLD;

    public static final double VOLUME_METER_MONTHLY_THRESHOLD =
        30.5 * VOLUME_METER_DAILY_THRESHOLD;

    public static final double VOLUME_AMPHIRO_MONTHLY_THRESHOLD =
        30.5 * VOLUME_AMPHIRO_DAILY_THRESHOLD;


    private EnumDayOfWeek tipDay = EnumDayOfWeek.MONDAY;

    private Period tipPeriod = Period.weeks(1);

    private Map<EnumDeviceType, Map<EnumTimeUnit, Integer>> budgetLimits =
        new EnumMap<>(EnumDeviceType.class);

    private Map<EnumDeviceType, Map<EnumTimeUnit, Double>> volumeThresholds =
        new EnumMap<>(EnumDeviceType.class);

    private boolean onDemandExecution = true;

    public Configuration()
    {
        Map<EnumTimeUnit, Integer> budget = null;

        budget = new EnumMap<EnumTimeUnit, Integer>(EnumTimeUnit.class);
        budget.put(EnumTimeUnit.DAY, BUDGET_METER_DAILY_LIMIT);
        budget.put(EnumTimeUnit.WEEK, BUDGET_METER_WEEKLY_LIMIT);
        budget.put(EnumTimeUnit.MONTH, BUDGET_METER_MONTHLY_LIMIT);
        budgetLimits.put(METER, budget);

        budget = new EnumMap<EnumTimeUnit, Integer>(EnumTimeUnit.class);
        budget.put(EnumTimeUnit.DAY, BUDGET_AMPHIRO_DAILY_LIMIT);
        budget.put(EnumTimeUnit.WEEK, BUDGET_AMPHIRO_WEEKLY_LIMIT);
        budget.put(EnumTimeUnit.MONTH, BUDGET_AMPHIRO_MONTHLY_LIMIT);
        budgetLimits.put(AMPHIRO, budget);

        Map<EnumTimeUnit, Double> threshold = null;

        threshold = new EnumMap<EnumTimeUnit, Double>(EnumTimeUnit.class);
        threshold.put(EnumTimeUnit.DAY, VOLUME_METER_DAILY_THRESHOLD);
        threshold.put(EnumTimeUnit.WEEK, VOLUME_METER_WEEKLY_THRESHOLD);
        threshold.put(EnumTimeUnit.MONTH, VOLUME_METER_MONTHLY_THRESHOLD);
        volumeThresholds.put(METER, threshold);

        threshold = new EnumMap<EnumTimeUnit, Double>(EnumTimeUnit.class);
        threshold.put(EnumTimeUnit.DAY, VOLUME_AMPHIRO_DAILY_THRESHOLD);
        threshold.put(EnumTimeUnit.WEEK, VOLUME_AMPHIRO_WEEKLY_THRESHOLD);
        threshold.put(EnumTimeUnit.MONTH, VOLUME_AMPHIRO_MONTHLY_THRESHOLD);
        volumeThresholds.put(AMPHIRO, threshold);
    }

    public Period getTipPeriod()
    {
        return tipPeriod;
    }

    public void setTipPeriod(Period period)
    {
        this.tipPeriod = period;
    }

    public void setTipPeriod(int days)
    {
        this.tipPeriod = Period.days(days);
    }

    public EnumDayOfWeek getTipDay()
    {
        return tipDay;
    }

    public void setTipDay(EnumDayOfWeek tipDay)
    {
        this.tipDay = tipDay;
    }

    public void setTipDay(int dayOfWeek)
    {
        this.tipDay = EnumDayOfWeek.valueOf(dayOfWeek);
    }

    public boolean isOnDemandExecution() {
        return onDemandExecution;
    }

    public void setOnDemandExecution(boolean flag) {
        this.onDemandExecution = flag;
    }

    public Integer getBudget(EnumDeviceType deviceType, EnumTimeUnit u)
    {
        Map<EnumTimeUnit, Integer> r = budgetLimits.get(deviceType);
        return (r == null)? null: r.get(u);
    }

    public Double getVolumeThreshold(EnumDeviceType deviceType, EnumTimeUnit u)
    {
        Map<EnumTimeUnit, Double> r = volumeThresholds.get(deviceType);
        return (r == null)? null: r.get(u);
    }

    public void setBudget(EnumDeviceType deviceType, EnumTimeUnit u, int budget)
    {
        Map<EnumTimeUnit, Integer> r = budgetLimits.get(deviceType);
        if (r == null) {
            r = new EnumMap<EnumTimeUnit, Integer>(EnumTimeUnit.class);
            budgetLimits.put(deviceType, r);
        }
        r.put(u, budget);
    }

    public void setVolumeThreshold(EnumDeviceType deviceType, EnumTimeUnit u, double threshold)
    {
        Map<EnumTimeUnit, Double> r = volumeThresholds.get(deviceType);
        if (r == null) {
            r = new EnumMap<EnumTimeUnit, Double>(EnumTimeUnit.class);
            volumeThresholds.put(deviceType, r);
        }
        r.put(u, threshold);
    }
}