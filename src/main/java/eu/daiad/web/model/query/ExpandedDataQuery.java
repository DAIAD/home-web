package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.EnumTimeAggregation;

public class ExpandedDataQuery {

    private boolean usingPreAggregation;

    private DateTimeZone timezone;

    private long startDateTime;

    private long endDateTime;

    private EnumTimeAggregation granularity;

    private ArrayList<ExpandedPopulationFilter> groups = new ArrayList<ExpandedPopulationFilter>();

    private List<EnumMetric> metrics;

    private ExpandedDataQuery() {
        metrics = new ArrayList<EnumMetric>();
        for (EnumMetric m : EnumMetric.values()) {
            if (m != EnumMetric.UNDEFINED) {
                metrics.add(m);
            }
        }
    }

    public ExpandedDataQuery(String timezone) {
        this(DateTimeZone.forID(timezone));
    }

    public ExpandedDataQuery(DateTimeZone timezone) {
        this();
        this.timezone = timezone;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(long endDateTime) {
        this.endDateTime = endDateTime;
    }

    public EnumTimeAggregation getGranularity() {
        return granularity;
    }

    public void setGranularity(EnumTimeAggregation granularity) {
        this.granularity = granularity;
    }

    public List<EnumMetric> getMetrics() {
        return metrics;
    }

    public ArrayList<ExpandedPopulationFilter> getGroups() {
        return groups;
    }

    public DateTimeZone getTimezone() {
        return timezone;
    }

    public boolean isUsingPreAggregation() {
        return usingPreAggregation;
    }

    public void setUsingPreAggregation(boolean usingPreAggregation) {
        this.usingPreAggregation = usingPreAggregation;
    }
}
