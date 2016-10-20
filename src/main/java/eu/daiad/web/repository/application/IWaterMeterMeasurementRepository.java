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

    void store(String serial, WaterMeterMeasurementCollection data);

    WaterMeterStatusQueryResult getStatus(String serials[]);

    WaterMeterStatusQueryResult getStatus(String serials[], long maxDateTime);

    WaterMeterMeasurementQueryResult searchMeasurements(String serials[], DateTimeZone timezone, WaterMeterMeasurementQuery query);

    ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

}
