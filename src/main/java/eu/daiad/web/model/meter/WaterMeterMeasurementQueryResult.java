package eu.daiad.web.model.meter;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.RestResponse;

public class WaterMeterMeasurementQueryResult extends RestResponse {

    private List<WaterMeterDataSeries> series = new ArrayList<WaterMeterDataSeries>();

    public WaterMeterMeasurementQueryResult() {
        super();
    }

    public WaterMeterMeasurementQueryResult(String code, String description) {
        super(code, description);
    }

    public List<WaterMeterDataSeries> getSeries() {
        return series;
    }
}
