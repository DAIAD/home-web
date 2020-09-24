package eu.daiad.common.model.meter;

import java.util.ArrayList;

public class WaterMeterForecastCollection {

    private ArrayList<WaterMeterForecast> measurements;

    public void setMeasurements(ArrayList<WaterMeterForecast> value) {
        measurements = value;
    }

    public ArrayList<WaterMeterForecast> getMeasurements() {
        return measurements;
    }

    public void add(long timestamp, Float difference) {
        if (measurements == null) {
            measurements = new ArrayList<WaterMeterForecast>();
        }
        WaterMeterForecast m = new WaterMeterForecast();
        m.setTimestamp(timestamp);
        m.setDifference(difference);
        measurements.add(m);
    }

}
