package eu.daiad.web.repository.application;

import java.util.List;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.GroupDataSeries;

public interface IMeterAggregateDataRepository {

    /**
     * Executes a query for smart water meter data using aggregates.
     *
     * @param query the query for filtering data.
     * @return a collection of {@link GroupDataSeries}.
     * @throws ApplicationException if an error occurs or query validation fails.
     */
    List<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException;

}
