package eu.daiad.web.model.query;

import static org.apache.commons.math3.stat.StatUtils.mean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTimeZone;
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

    public ArrayList<GroupDataSeries> getSeries(EnumDeviceType t)
    {
        return (t == EnumDeviceType.AMPHIRO)? devices : meters;
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
     * This is a convenience method for the common case where only 1 data point is contained per
     * device (e.g. when aggregation interval is same as the sliding interval).
     *
     * Checks that actually only a single data point exists per device. If more than 1 devices
     * are contained in this response, returns the average of them.
     *
     * @return a boxed scalar result
     */
    public Double getSingleResult(EnumDeviceType deviceType, EnumDataField field, EnumMetric metric)
    {
        // Collect single values for each one of the devices of the given device type

        List<Double> values = new ArrayList<>(12);

        switch (deviceType) {
        case AMPHIRO:
            for (GroupDataSeries s: devices) {
                List<DataPoint> points = s.getPoints();
                if (!points.isEmpty()) {
                    Assert.state(points.size() == 1, "Expected a single data point");
                    AmphiroDataPoint p0 = (AmphiroDataPoint) points.get(0);
                    Map<EnumMetric, Double> metrics = null;
                    switch (field) {
                    case DURATION:
                        metrics = p0.getDuration();
                        break;
                    case ENERGY:
                        metrics = p0.getEnergy();
                        break;
                    case FLOW:
                        metrics = p0.getFlow();
                        break;
                    case TEMPERATURE:
                        metrics = p0.getTemperature();
                        break;
                    case VOLUME:
                    default:
                        metrics = p0.getVolume();
                        break;
                    }
                    values.add(metrics.get(metric));
                }
            }
            break;
        case METER:
        default:
            for (GroupDataSeries s: meters) {
                List<DataPoint> points = s.getPoints();
                if (!points.isEmpty()) {
                    Assert.state(points.size() == 1, "Expected a single data point");
                    MeterDataPoint p0 = (MeterDataPoint) points.get(0);
                    values.add(p0.getVolume().get(metric));
                }
            }
            break;
        }

        // Average over devices

        int n = values.size();
        return (n > 0)?
            mean(ArrayUtils.toPrimitive(values.toArray(new Double[n]))) : null;
    }
}
