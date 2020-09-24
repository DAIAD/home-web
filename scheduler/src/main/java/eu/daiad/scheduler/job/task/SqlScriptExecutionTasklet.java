package eu.daiad.scheduler.job.task;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;
import org.thymeleaf.util.StringUtils;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.scheduling.Constants;

/**
 * Task for executing SQL scripts/
 */
public class SqlScriptExecutionTasklet implements StoppableTasklet {

    /**
     * Spring application context.
     */
    @Autowired
    protected ApplicationContext applicationContext;

    /**
     * Resolves application messages and supports internationalization.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * Spring JDBC template.
     */
	private JdbcTemplate jdbcTemplate;

	/**
	 * List of SQL script resources.
	 */
	private final List<Resource> locations = new ArrayList<Resource>();

	/**
	 * Name for SQL script location parameter.
	 */
	private String locationParameter = "locations";

	/**
	 * Synchronizes requests for stopping the task.
	 */
	private AtomicBoolean stopped = new AtomicBoolean(false);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		setLocations(chunkContext.getStepContext());

		for (Resource resource : locations) {
			if (stopped.get()) {
				break;
			} else {
				try (InputStream stream = resource.getInputStream()) {
					String sql = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);

					jdbcTemplate.execute(sql);
				}
			}
		}

		return RepeatStatus.FINISHED;
	}

	@Override
	public void stop() {
		stopped.set(true);
	}

	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

    private ApplicationException createApplicationException(ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.create(code, pattern);

    }

	private void setLocations(StepContext stepContext) {
		String locationParameterValue = (String) stepContext.getJobParameters().get(stepContext.getStepName() + Constants.PARAMETER_NAME_DELIMITER + locationParameter);

		String[] locations = StringUtils.split(locationParameterValue, ";");

		for (String location : locations) {
			Resource resource = applicationContext.getResource(location);
			if (resource.exists()) {
				this.locations.add(resource);
			} else {
				throw createApplicationException(SharedErrorCode.RESOURCE_DOES_NOT_EXIST).set("resource", location);
			}
		}
	}

	public void setLocationParameter(String locationParameter) {
		this.locationParameter = locationParameter;
	}

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}