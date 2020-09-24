package eu.daiad.utility.service.etl;

/**
 * Base class for data export queries.
 */
public abstract class DataExportQuery {

    /**
     * Export time interval start time stamp.
     */
    private Long startTimestamp;

    /**
     * Export time interval end time stamp.
     */
    private Long endTimestamp;

    /**
     * Exported data time zone.
     */
    private String timezone;

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
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
