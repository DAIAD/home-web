package eu.daiad.web.service.etl;

/**
 * Base class for data export queries.
 */
public abstract class DataExportQuery {

    /**
     * Export time interval start time stamp.
     */
    private Long startTimstamp;

    /**
     * Export time interval end time stamp.
     */
    private Long endTimestamp;

    /**
     * Exported data time zone.
     */
    private String timezone;

    public Long getStartTimstamp() {
        return startTimstamp;
    }

    public void setStartTimstamp(Long startTimstamp) {
        this.startTimstamp = startTimstamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

}
