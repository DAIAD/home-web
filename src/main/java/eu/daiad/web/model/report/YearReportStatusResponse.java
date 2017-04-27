package eu.daiad.web.model.report;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class YearReportStatusResponse extends RestResponse {

    private List<ReportStatus> reports;

    public YearReportStatusResponse(List<ReportStatus> reports) {
        super();

        this.reports = reports;
    }

    public List<ReportStatus> getReports() {
        return reports;
    }
}
