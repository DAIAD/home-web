package eu.daiad.common.repository.application;

import java.util.List;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.query.ExpandedDataQuery;
import eu.daiad.common.model.query.GroupDataSeries;

public interface IMeterForecastingAggregateDataRepository {

    /**
     * Executes a query for smart water meter forecasting data using aggregates.
     *
     * @param query the query for filtering data.
     * @return a collection of {@link GroupDataSeries}.
     * @throws ApplicationException if an error occurs or query validation fails.
     */
    List<GroupDataSeries> forecast(ExpandedDataQuery query) throws ApplicationException;
}
