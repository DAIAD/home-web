package eu.daiad.web.repository.application;

import java.nio.file.Paths;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import eu.daiad.web.repository.BaseRepository;

/**
 * Provides methods for accessing application spatial data.
 */
@Repository
public class JpaReportRepository extends BaseRepository implements IReportRepository {

    /**
     * Java Persistence entity manager.
     */
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Folder with reports.
     */
    @Value("${daiad.report.path}")
    private String reportPath;

    /**
     * Returns the path of an existing report.
     *
     * @param username the name of the report owner.
     * @param year the reference year.
     * @param month the reference month.
     * @return the report path if it exists; Otherwise null is returned.
     */
    @Override
    public String getReportPath(String username, int year, int month) {
        DateTime refDate = new DateTime(year, month, 1, 0, 0, 0);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

        String from = refDate.dayOfMonth().withMinimumValue().toString(formatter);
        String to = refDate.dayOfMonth().withMaximumValue().toString(formatter);

        String outputDirectory = Paths.get(reportPath,
                                          Integer.toString(refDate.dayOfMonth()
                                                                  .withMinimumValue()
                                                                  .getYear()),
                                          Integer.toString(refDate.dayOfMonth()
                                                                  .withMinimumValue()
                                                                  .getMonthOfYear())).toString();

        return Paths.get(outputDirectory, username + "-" + from + "-" + to + ".pdf").toString();
    }

}