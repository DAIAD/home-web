package eu.daiad.web.service.scheduling;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;
import org.springframework.stereotype.Component;

@Component
public class DataSourceJobParametersIncrementer implements JobParametersIncrementer, DataFieldMaxValueIncrementer,
                InitializingBean {

    @Value("${job.parameters.incrementer.name}")
    String incrementerName;

    @Autowired
    private DataSource dataSource;

    private PostgreSQLSequenceMaxValueIncrementer incrementer = new PostgreSQLSequenceMaxValueIncrementer();

    @Override
    public void afterPropertiesSet() throws Exception {
        this.incrementer.setDataSource(this.dataSource);
        this.incrementer.setIncrementerName(incrementerName);

    }

    @Override
    public JobParameters getNext(JobParameters parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return new JobParametersBuilder().addLong("run.id", incrementer.nextLongValue()).toJobParameters();
        }

        Map<String, JobParameter> map = new HashMap<String, JobParameter>(parameters.getParameters());
        map.put("run.id", new JobParameter(incrementer.nextLongValue()));

        return new JobParameters(map);
    }

    @Override
    public int nextIntValue() throws DataAccessException {
        return (int) incrementer.nextLongValue();
    }

    @Override
    public long nextLongValue() throws DataAccessException {
        return incrementer.nextLongValue();
    }

    @Override
    public String nextStringValue() throws DataAccessException {
        return Long.toString(incrementer.nextLongValue());
    }

}
