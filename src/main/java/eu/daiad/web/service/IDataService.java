package eu.daiad.web.service;

import java.util.List;

import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.ForecastQuery;
import eu.daiad.web.model.query.ForecastQueryResponse;
import eu.daiad.web.model.query.NamedDataQuery;

public interface IDataService {

	/**
	 * Executes a generic query for Amphiro B1 sessions and smart water meter readings.
	 * 
	 * @param query the query to execute.
	 * @return A collection of data series.
	 */
	abstract DataQueryResponse execute(DataQuery query);

    /**
     * Executes a generic query for smart water meter forecasting.
     * 
     * @param query the query to execute.
     * @return A collection of data series.
     */
    abstract ForecastQueryResponse execute(ForecastQuery query);
    
    /**
     * Stores a data query and assigns a unique name to it.
     * 
     * @param query the data query along with label and tags
     */
    abstract void storeQuery(NamedDataQuery query);
 
    /**
     * Loads all saved queries.
     * 
     * @return the queries.
     */
    abstract List<NamedDataQuery> getAllQueries();
    
}
