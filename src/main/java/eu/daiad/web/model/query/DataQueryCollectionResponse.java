package eu.daiad.web.model.query;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class DataQueryCollectionResponse extends RestResponse {

    List<NamedDataQuery> queries;

    public List<NamedDataQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<NamedDataQuery> queries) {
        this.queries = queries;
    }

}
