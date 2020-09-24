package eu.daiad.common.model.query;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTimeZone;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.spatial.LabeledGeometry;

public abstract class QueryResponse extends RestResponse {

    private ExecutionInfo execution = new ExecutionInfo();

    private String timezone;

    private Map<Long, LabeledGeometry> areas = new HashMap<Long, LabeledGeometry>();

    public QueryResponse() {
        timezone = DateTimeZone.UTC.toString();
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

    public ExecutionInfo getExecution() {
        return execution;
    }

    public static class ExecutionInfo {

        private long duration;

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

    }
}
