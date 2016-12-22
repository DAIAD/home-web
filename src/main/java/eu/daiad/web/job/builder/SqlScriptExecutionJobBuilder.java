package eu.daiad.web.job.builder;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.ConsumptionClusterTask;
import eu.daiad.web.job.task.SqlScriptExecutionTasklet;

/**
 * Helper builder class for initializing a job that executes a list of SQL
 * commands.
 */
@Component
public class SqlScriptExecutionJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * A (;) delimited list of SQL script paths.
     */
    private static final String PARAMETER_LOCATIONS = "locations";

    /**
     * Data source for executing SQL commands.
     */
    @Autowired
    @Qualifier("applicationDataSource")
    private DataSource dataSource;

    /**
     * Task that clusters users based on their consumption and computes water IQ status.
     */
    @Autowired
    private ConsumptionClusterTask consumptionClusterStep;

    private Step executeScripts() {
        SqlScriptExecutionTasklet sqlScriptExecutionTasklet = new SqlScriptExecutionTasklet();

        sqlScriptExecutionTasklet.setApplicationContext(applicationContext);
        sqlScriptExecutionTasklet.setDataSource(dataSource);
        sqlScriptExecutionTasklet.setLocationParameter(PARAMETER_LOCATIONS);

        return stepBuilderFactory.get("executeScripts").tasklet(sqlScriptExecutionTasklet).build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name).incrementer(incrementer).start(executeScripts()).build();
    }
}
