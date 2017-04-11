package eu.daiad.web.repository.application;


/**
 * Provides methods for accessing user reports.
 */
public interface IReportRepository {

    /**
     * Returns the path of an existing report.
     *
     * @param username the name of the report owner.
     * @param year the reference year.
     * @param month the reference month.
     * @return the report path if it exists; Otherwise null is returned.
     */
    String getReportPath(String username, int year, int month);

}
