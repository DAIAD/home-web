package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import eu.daiad.web.model.device.EnumDeviceType;

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

    public ArrayList<GroupDataSeries> getSeries(EnumDeviceType deviceType)
    {
        return (deviceType == EnumDeviceType.AMPHIRO)? devices : meters;
    }

    public void setDevices(ArrayList<GroupDataSeries> devices) {
        this.devices = devices;
    }

    public void setMeters(ArrayList<GroupDataSeries> meters) {
        this.meters = meters;
    }

    /**
     * Get a single scalar result from this query response.
     *
     * This is a convenience method for the common case where only 1 series with a single
     * data point is contained per device (e.g. when aggregation interval is same as the 
     * sliding interval).
     */
    public Double toNumber(EnumDeviceType deviceType, EnumDataField field, EnumMetric metric)
    {
        List<GroupDataSeries> series = getSeries(deviceType);
        if (series.isEmpty())
            return null;
        
        Assert.state(series.size() == 1, "Expected 1 series per device!");
        return series.get(0).toNumber(field, metric);
    }
    
    /**
     * Get an iterator on (time, value) pairs from this query response.
     * 
     * This is a convenience method for the common case when only 1 series (e.g a single population
     * filter) is contained per device. 
     */
    public Iterable<Point> iterPoints(EnumDeviceType deviceType, EnumDataField field, EnumMetric metric)
    {
        List<GroupDataSeries> series = getSeries(deviceType);
        if (series.isEmpty())
            return Collections.emptyList();
        
        Assert.state(series.size() == 1, "Expected 1 series per device!");
        return series.get(0).iterPoints(field, metric);
    }
}
