package eu.daiad.web.model.query;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.spatial.LabeledGeometry;

public abstract class QueryResponse extends RestResponse {

    private String timezone;

    private Map<Long, LabeledGeometry> areas = new HashMap<Long, LabeledGeometry>();

    public QueryResponse() {
        this.timezone = DateTimeZone.UTC.toString();
    }

    public QueryResponse(String timezone) {
        this.timezone = timezone;
    }

    public QueryResponse(DateTimeZone timezone) {
        this.timezone = timezone.toString();
    }

    public String getTimezone() {
        return timezone;
    }

    public Map<Long, LabeledGeometry> getAreas() {
        return areas;
    }
}
