package eu.daiad.web.model.meter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import eu.daiad.web.model.DeviceMeasurementCollection;
import eu.daiad.web.model.device.EnumDeviceType;

public class WaterMeterMeasurementCollection extends DeviceMeasurementCollection {

    private List<WaterMeterMeasurement> measurements = new ArrayList<WaterMeterMeasurement>();

    public WaterMeterMeasurementCollection() {

    }

    public WaterMeterMeasurementCollection(List<WaterMeterMeasurement> measurements) {
        this.measurements = measurements;

        Assert.notNull(this.measurements);
    }

    public List<WaterMeterMeasurement> getMeasurements() {
        return this.measurements;
    }

    public void add(long timestamp, float volume, Float difference) {
        WaterMeterMeasurement measurement = new WaterMeterMeasurement();

        measurement.setTimestamp(timestamp);
        measurement.setVolume(volume);
        measurement.setDifference(difference);

        this.measurements.add(measurement);
    }

    @Override
    public EnumDeviceType getType() {
        return EnumDeviceType.METER;
    }

}
