package eu.daiad.common.model.meter;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class WaterMeterMeasurementQueryResult extends RestResponse {

    private List<WaterMeterDataSeries> series = new ArrayList<WaterMeterDataSeries>();

    public WaterMeterMeasurementQueryResult() {
        super();
    }

    public WaterMeterMeasurementQueryResult(ErrorCode code, String description) {
        super(code, description);
    }

    public List<WaterMeterDataSeries> getSeries() {
        return series;
    }
}
