package eu.daiad.web.service;

import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryResponse;

public interface IDataService {

	abstract DataQueryResponse execute(DataQuery query);

}
