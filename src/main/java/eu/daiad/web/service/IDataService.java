package eu.daiad.web.service;

import java.util.List;
import java.util.UUID;
import java.io.IOException;

import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.ForecastQuery;
import eu.daiad.web.model.query.ForecastQueryResponse;
import eu.daiad.web.model.query.NamedDataQuery;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonParseException;

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
     * @param key the user's UUID key
     */
    abstract void storeQuery(NamedDataQuery query, UUID key);
    
    /**
     * Updates a stored data query.
     * 
     * @param query the data query along with label and tags
     * @param key the user's UUID key
     */
    abstract void updateStoredQuery(NamedDataQuery query, UUID key);    
    
    
    /**
     * Stores a data query and assigns a unique name to it.
     * 
     * @param query the data query along with label and tags
     * @param username the user's name
     */
    abstract void storeQuery(NamedDataQuery query, String username);    

    /**
     * Deletes a data query.
     * 
     * @param query the query
     * @param key the user's UUID key
     */
    abstract void deleteStoredQuery(NamedDataQuery query, UUID key);   

    /**
     * Pins a data query to dashboard.
     * 
     * @param id the query id
     * @param key the user's UUID key
     */
    abstract void pinStoredQuery(long id, UUID key); 
    
    /**
     * Loads user's saved queries.
     *  
     * @param accountId the user's account id
     * @return the queries.
     * @throws com.fasterxml.jackson.databind.JsonMappingException
     * @throws com.fasterxml.jackson.core.JsonParseException
     */
    abstract List<NamedDataQuery> getQueriesForOwner(int accountId) 
            throws JsonMappingException, JsonParseException, IOException;
    
    /**
     * Loads all saved queries.
     * 
     * @return the queries.
     */    
    abstract List<NamedDataQuery> getAllQueries();
    
}
