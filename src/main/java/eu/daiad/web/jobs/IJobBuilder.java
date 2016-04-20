package eu.daiad.web.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;

public interface IJobBuilder {

	abstract Job build(String name, JobParametersIncrementer incrementer) throws Exception;
	
}
