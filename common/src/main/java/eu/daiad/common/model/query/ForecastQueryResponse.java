package eu.daiad.common.model.query;

import java.util.List;

import org.joda.time.DateTimeZone;

public class ForecastQueryResponse extends QueryResponse {

    private List<GroupDataSeries> meters;

    public ForecastQueryResponse() {
        super();
    }

    public ForecastQueryResponse(String timezone) {
        super(timezone);
    }

    public ForecastQueryResponse(DateTimeZone timezone) {
        super(timezone.toString());
    }

    public List<GroupDataSeries> getMeters() {
        return meters;
    }

    public void setMeters(List<GroupDataSeries> meters) {
        this.meters = meters;
    }

}
