package eu.daiad.web.model.meter;

import java.util.ArrayList;
import java.util.UUID;

public class WaterMeterForecastCollection {

    private UUID deviceKey;

    private ArrayList<WaterMeterForecast> measurements;

    public void setMeasurements(ArrayList<WaterMeterForecast> value) {
        this.measurements = value;
    }

    public ArrayList<WaterMeterForecast> getMeasurements() {
        return this.measurements;
    }

    public UUID getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(UUID deviceKey) {
        this.deviceKey = deviceKey;
    }

}
