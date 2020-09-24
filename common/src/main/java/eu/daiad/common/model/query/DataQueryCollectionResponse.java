package eu.daiad.common.model.query;

import java.util.List;

import eu.daiad.common.model.RestResponse;

public class DataQueryCollectionResponse extends RestResponse {

    List<NamedDataQuery> queries;

    public List<NamedDataQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<NamedDataQuery> queries) {
        this.queries = queries;
    }

}
