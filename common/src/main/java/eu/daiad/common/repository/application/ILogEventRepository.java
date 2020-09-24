package eu.daiad.common.repository.application;

import eu.daiad.common.model.logging.LogEventQuery;
import eu.daiad.common.model.logging.LogEventQueryResult;

public interface ILogEventRepository {

	/**
	 * Returns logged events. Optionally filters result based on the given query.
	 * 
	 * @param query the query.
	 * @return the logged events.
	 */
	abstract LogEventQueryResult getLogEvents(LogEventQuery query);
}
