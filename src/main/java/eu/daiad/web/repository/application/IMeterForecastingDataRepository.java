package eu.daiad.web.repository.application;

import java.util.List;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.meter.WaterMeterForecastCollection;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.GroupDataSeries;

public interface IMeterForecastingDataRepository {

    /**
     * Stores smart water meter forecasting data.
     *
     * @param serial the smart water meter unique serial number.
     * @param data a collection of {@link WaterMeterForecastCollection}.
     */
    void store(String serial, WaterMeterForecastCollection data);

    /**
     * Executes a query for smart water meter forecasting data.
     *
     * @param query the query for filtering data.
     * @return a collection of {@link GroupDataSeries}.
     * @throws ApplicationException if an error occurs or query validation fails.
     */
    List<GroupDataSeries> forecast(ExpandedDataQuery query) throws ApplicationException;
}
