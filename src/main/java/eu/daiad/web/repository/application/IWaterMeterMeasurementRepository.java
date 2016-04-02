package eu.daiad.web.repository.application;

import java.io.IOException;

import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;

public interface IWaterMeterMeasurementRepository {

	public abstract void storeData(String serial, WaterMeterMeasurementCollection data);

	public abstract WaterMeterStatusQueryResult getStatus(String serials[], WaterMeterStatusQuery query);

	public abstract WaterMeterMeasurementQueryResult searchMeasurements(String serials[],
					WaterMeterMeasurementQuery query);

	public abstract void open() throws IOException;
	
	public abstract void close();
	
	public abstract boolean isOpen();

}