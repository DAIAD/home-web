package eu.daiad.common.model.query;

import java.util.List;

import org.joda.time.DateTimeZone;

import eu.daiad.common.model.device.EnumDeviceType;

public class DataQueryResponse extends QueryResponse {

    private List<GroupDataSeries> devices;

    private List<GroupDataSeries> meters;

    public DataQueryResponse() {
        super();
    }

    public DataQueryResponse(String timezone) {
        super(timezone);
    }

    public DataQueryResponse(DateTimeZone timezone) {
        super(timezone.toString());
    }

    public List<GroupDataSeries> getDevices() {
        return devices;
    }

    public List<GroupDataSeries> getMeters() {
        return meters;
    }

    public List<GroupDataSeries> getSeries(EnumDeviceType deviceType) {
        return (deviceType == EnumDeviceType.AMPHIRO) ? devices : meters;
    }

    public void setDevices(List<GroupDataSeries> devices) {
        this.devices = devices;
    }

    public void setMeters(List<GroupDataSeries> meters) {
        this.meters = meters;
    }

    public SeriesFacade getFacade(EnumDeviceType deviceType, int seriesIndex) {
        List<GroupDataSeries> series = getSeries(deviceType);
        GroupDataSeries sx = (seriesIndex < series.size()) ? series.get(seriesIndex) : null;
        return (sx != null) ? sx.newFacade() : null;
    }

    public SeriesFacade getFacade(EnumDeviceType deviceType, String label) {
        List<GroupDataSeries> series = getSeries(deviceType);
        if (series.isEmpty())
            return null;

        GroupDataSeries sx = null;
        for (GroupDataSeries s : series)
            if (s.getLabel().equalsIgnoreCase(label)) {
                sx = s;
                break;
            }
        return (sx != null) ? sx.newFacade() : null;
    }

    public SeriesFacade getFacade(EnumDeviceType deviceType) {
        return getFacade(deviceType, 0);
    }
}
