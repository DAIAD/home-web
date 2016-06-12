package eu.daiad.web.model.query;

import java.util.ArrayList;

import org.joda.time.DateTimeZone;

public class DataQueryResponse extends QueryResponse {

    private ArrayList<GroupDataSeries> devices;

    private ArrayList<GroupDataSeries> meters;

    public DataQueryResponse() {
        super();
    }

    public DataQueryResponse(String timezone) {
        super(timezone);
    }

    public DataQueryResponse(DateTimeZone timezone) {
        super(timezone.toString());
    }

    public ArrayList<GroupDataSeries> getDevices() {
        return devices;
    }

    public ArrayList<GroupDataSeries> getMeters() {
        return meters;
    }

    public void setDevices(ArrayList<GroupDataSeries> devices) {
        this.devices = devices;
    }

    public void setMeters(ArrayList<GroupDataSeries> meters) {
        this.meters = meters;
    }

}
