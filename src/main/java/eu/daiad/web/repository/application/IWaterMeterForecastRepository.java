package eu.daiad.web.repository.application;

import java.util.ArrayList;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.meter.WaterMeterForecastCollection;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.GroupDataSeries;

public interface IWaterMeterForecastRepository {

    abstract void store(String serial, WaterMeterForecastCollection data);

    abstract ArrayList<GroupDataSeries> forecast(ExpandedDataQuery query) throws ApplicationException;
}
