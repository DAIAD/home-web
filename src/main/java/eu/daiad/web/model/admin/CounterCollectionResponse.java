package eu.daiad.web.model.admin;

import java.util.Map;

import eu.daiad.web.model.RestResponse;

public class CounterCollectionResponse extends RestResponse {

    private Map<String, Counter> counters;

    public Map<String, Counter> getCounters() {
        return counters;
    }

    public void setCounters(Map<String, Counter> counters) {
        this.counters = counters;
    }

}
