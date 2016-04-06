package eu.daiad.web.jobs;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.admin.Job;
import eu.daiad.web.domain.admin.ScheduledJob;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;

@Repository
@Transactional("managementTransactionManager")
public class SchedulerRepository implements ISchedulerRepository {

	@PersistenceContext(unitName = "management")
	EntityManager entityManager;

	@Override
	public Job getJobByName(String name) {
		try {
			TypedQuery<eu.daiad.web.domain.admin.Job> query = entityManager
							.createQuery("select j from job j where j.jobName = :job_name",
											eu.daiad.web.domain.admin.Job.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("job_name", name);

			return query.getSingleResult();
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public List<ScheduledJob> getScheduledJobs() {
		try {
			TypedQuery<eu.daiad.web.domain.admin.ScheduledJob> query = entityManager.createQuery(
							"select j from scheduled_job j", eu.daiad.web.domain.admin.ScheduledJob.class);

			return query.getResultList();
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

}
