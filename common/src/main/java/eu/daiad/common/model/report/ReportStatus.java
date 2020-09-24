package eu.daiad.common.model.report;

import org.joda.time.DateTime;

public class ReportStatus {

    private DateTime createdOn;

    private long size;

    private String url;

    private int year;

    private int month;

    public ReportStatus(DateTime createdOn, long size, String url, int year, int month) {
        this.createdOn = createdOn;
        this.size = size;
        this.url = url;
        this.year = year;
        this.month = month;
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

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

}
