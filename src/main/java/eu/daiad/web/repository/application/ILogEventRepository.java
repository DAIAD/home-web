package eu.daiad.web.repository.application;

import eu.daiad.web.model.logging.LogEventQuery;
import eu.daiad.web.model.logging.LogEventQueryResult;

public interface ILogEventRepository {

	/**
	 * Returns logged events. Optionally filters result based on the given query.
	 * 
	 * @param query the query.
	 * @return the logged events.
	 */
	abstract LogEventQueryResult getLogEvents(LogEventQuery query);
}
