package eu.daiad.web.data;

import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;

public interface IWaterMeterMeasurementRepository {

	public abstract void storeData(WaterMeterMeasurementCollection data);

	public abstract WaterMeterStatusQueryResult getStatus(WaterMeterStatusQuery query);

	public abstract WaterMeterMeasurementQueryResult searchMeasurements(WaterMeterMeasurementQuery query);

}