package eu.daiad.web.repository.application;

import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.GroupDataSeries;

public interface IWaterMeterMeasurementRepository {

	public abstract void storeData(String serial, WaterMeterMeasurementCollection data);

	public abstract WaterMeterStatusQueryResult getStatus(String serials[]);

	public abstract WaterMeterStatusQueryResult getStatus(String serials[], long maxDateTime);

	public abstract WaterMeterMeasurementQueryResult searchMeasurements(String serials[], DateTimeZone timezone,
					WaterMeterMeasurementQuery query);

	public abstract void open() throws IOException;

	public abstract void close();

	public abstract boolean isOpen();

	public abstract ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

}