package eu.daiad.web.model.meter;

import eu.daiad.web.model.TemporalConstants;

public class WaterMeterMeasurementQuery extends WaterMeterStatusQuery {

    private int granularity = TemporalConstants.NONE;

    private Long startDate;

    private Long endDate;

    public int getGranularity() {
        return granularity;
    }

    public void setGranularity(int granularity) {
        this.granularity = granularity;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

}
