package eu.daiad.web.model.report;

import org.joda.time.DateTime;

public class ReportStatus {

    private DateTime createdOn;

    private long size;

    private String url;

    public ReportStatus(DateTime createdOn, long size, String url) {
        this.createdOn = createdOn;
        this.size = size;
        this.url = url;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

}
