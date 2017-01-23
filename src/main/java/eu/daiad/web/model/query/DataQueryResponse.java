package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeZone;

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

    public SeriesFacade getFacade(EnumDeviceType deviceType, int seriesIndex)
    {
        List<GroupDataSeries> series = getSeries(deviceType);
        GroupDataSeries sx = (seriesIndex < series.size())? series.get(seriesIndex) : null;
        return (sx != null)? sx.newFacade() : null;
    }

    public SeriesFacade getFacade(EnumDeviceType deviceType, String label)
    {
        List<GroupDataSeries> series = getSeries(deviceType);
        if (series.isEmpty())
            return null;

        GroupDataSeries sx = null;
        for (GroupDataSeries s: series)
            if (s.getLabel().equalsIgnoreCase(label)) {
                sx = s;
                break;
            }
        return (sx != null)? sx.newFacade() : null;
    }

    public SeriesFacade getFacade(EnumDeviceType deviceType)
    {
        return getFacade(deviceType, 0);
    }
}
