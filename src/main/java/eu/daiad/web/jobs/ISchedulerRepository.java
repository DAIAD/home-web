package eu.daiad.web.jobs;

import java.util.List;

import eu.daiad.web.domain.admin.Job;
import eu.daiad.web.domain.admin.ScheduledJob;

public interface ISchedulerRepository {

	abstract Job getJobByName(String name);

	abstract List<ScheduledJob> getScheduledJobs();

}
