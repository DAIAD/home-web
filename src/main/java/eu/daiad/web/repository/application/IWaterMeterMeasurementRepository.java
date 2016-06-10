package eu.daiad.web.repository.application;

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

    abstract void store(String serial, WaterMeterMeasurementCollection data);

    abstract WaterMeterStatusQueryResult getStatus(String serials[]);

    abstract WaterMeterStatusQueryResult getStatus(String serials[], long maxDateTime);

    abstract WaterMeterMeasurementQueryResult searchMeasurements(String serials[], DateTimeZone timezone,
                    WaterMeterMeasurementQuery query);

    abstract ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

}