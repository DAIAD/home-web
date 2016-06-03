package eu.daiad.web.service.scheduling.tasklet;

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
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;
import org.thymeleaf.util.StringUtils;

import eu.daiad.web.model.error.SharedErrorCode;

public class SqlScriptExecutionTasklet extends BaseTasklet implements StoppableTasklet {

	private JdbcTemplate jdbcTemplate;

	private final List<Resource> locations = new ArrayList<Resource>();

	private String locationParameter = "locations";

	private AtomicBoolean stopped = new AtomicBoolean(false);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		this.setLocations(chunkContext.getStepContext());

		for (Resource resource : this.locations) {
			if (this.stopped.get()) {
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
		this.stopped.set(true);
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private void setLocations(StepContext stepContext) {
		String locationParameterValue = (String) stepContext.getJobParameters().get(this.locationParameter);

		String[] locations = StringUtils.split(locationParameterValue, ";");

		for (String location : locations) {
			Resource resource = this.applicationContext.getResource(location);
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
}
