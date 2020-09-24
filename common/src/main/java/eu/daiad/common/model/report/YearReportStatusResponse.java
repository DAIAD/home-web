package eu.daiad.common.model.report;

import java.util.List;

import eu.daiad.common.model.RestResponse;

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
