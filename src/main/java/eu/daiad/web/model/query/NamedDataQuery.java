package eu.daiad.web.model.query;

public class NamedDataQuery {

    private String title;

    private DataQuery query;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DataQuery getQuery() {
        return query;
    }

    public void setQuery(DataQuery query) {
        this.query = query;
    }

}
