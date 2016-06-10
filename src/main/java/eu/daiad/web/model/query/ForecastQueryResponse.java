package eu.daiad.web.model.query;

import java.util.ArrayList;

import org.joda.time.DateTimeZone;

public class ForecastQueryResponse extends QueryResponse {

    private ArrayList<GroupDataSeries> meters;

    public ForecastQueryResponse() {
        super();
    }

    public ForecastQueryResponse(String timezone) {
        super(timezone);
    }

    public ForecastQueryResponse(DateTimeZone timezone) {
        super(timezone.toString());
    }

    public ArrayList<GroupDataSeries> getMeters() {
        return meters;
    }

    public void setMeters(ArrayList<GroupDataSeries> meters) {
        this.meters = meters;
    }

}
