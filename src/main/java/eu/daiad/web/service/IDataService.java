package eu.daiad.web.service;

import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryResponse;

public interface IDataService {

	/**
	 * Executes a generic query for Amphiro B1 sessions and smart water meter readings.
	 * 
	 * @param query the query to execute.
	 * @return A collection of data series.
	 */
	abstract DataQueryResponse execute(DataQuery query);

}
