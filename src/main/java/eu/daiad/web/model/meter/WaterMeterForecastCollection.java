package eu.daiad.web.model.meter;

import java.util.ArrayList;

public class WaterMeterForecastCollection {

    private ArrayList<WaterMeterForecast> measurements;

    public void setMeasurements(ArrayList<WaterMeterForecast> value) {
        measurements = value;
    }

    public ArrayList<WaterMeterForecast> getMeasurements() {
        return measurements;
    }

}
