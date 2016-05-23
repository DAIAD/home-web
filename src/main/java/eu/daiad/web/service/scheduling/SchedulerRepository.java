package eu.daiad.web.service.scheduling;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.admin.ScheduledJob;
import eu.daiad.web.domain.admin.ScheduledJobExecution;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;

@Repository
@Transactional("managementTransactionManager")
public class SchedulerRepository implements ISchedulerRepository {

	@PersistenceContext(unitName = "management")
	EntityManager entityManager;

	@Override
	public List<ScheduledJob> getJobs() {
		try {
			TypedQuery<ScheduledJob> query = entityManager.createQuery("select j from scheduled_job j",
							ScheduledJob.class);

			return query.getResultList();
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public ScheduledJob getJobById(long jobId) {
		try {
			TypedQuery<ScheduledJob> query = entityManager.createQuery("select j from scheduled_job j where j.id= :id",
							ScheduledJob.class);

			query.setParameter("id", jobId);

			List<ScheduledJob> result = query.getResultList();

			if (!result.isEmpty()) {
				return result.get(0);
			}

			return null;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public ScheduledJob getJobByName(String jobName) {
		try {
			TypedQuery<ScheduledJob> query = entityManager.createQuery("select j from scheduled_job j where j.name= :jobName",
							ScheduledJob.class);

			query.setParameter("jobName", jobName);

			List<ScheduledJob> result = query.getResultList();

			if (!result.isEmpty()) {
				return result.get(0);
			}

			return null;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public List<ScheduledJobExecution> getExecutions(String jobName, int startPosition, int maxResult) {
		TypedQuery<ScheduledJobExecution> query = entityManager
						.createQuery("select e from scheduled_job_execution e "
										+ "where e.jobName = :jobName e.jobInstanceId desc, e.jobExecutionId desc",
										ScheduledJobExecution.class).setMaxResults(maxResult)
						.setFirstResult(startPosition);

		query.setParameter("jobName", jobName);

		return query.getResultList();
	}

	@Override
	public List<ScheduledJobExecution> getExecutions(long jobId, int startPosition, int size) {
		ScheduledJob job = this.getJobById(jobId);

		TypedQuery<ScheduledJobExecution> query = entityManager.createQuery("select e from scheduled_job_execution e "
						+ "where e.jobName = :jobName e.jobInstanceId desc, e.jobExecutionId desc",
						ScheduledJobExecution.class);

		query.setParameter("jobName", job.getName());

		return query.getResultList();
	}

	@Override
	public ScheduledJobExecution getLastExecution(String jobName) {
		TypedQuery<ScheduledJobExecution> query = entityManager
						.createQuery("select e from scheduled_job_execution e "
										+ "where e.jobName = :jobName e.jobInstanceId desc, e.jobExecutionId desc",
										ScheduledJobExecution.class).setMaxResults(1).setFirstResult(0);

		query.setParameter("job_name", jobName);

		List<ScheduledJobExecution> result = query.getResultList();

		if (!result.isEmpty()) {
			return result.get(0);
		}

		return null;
	}

	@Override
	public ScheduledJobExecution getLastExecution(long jobId) {
		ScheduledJob job = this.getJobById(jobId);

		TypedQuery<ScheduledJobExecution> query = entityManager
						.createQuery("select e from scheduled_job_execution e "
										+ "where e.jobName = :jobName e.jobInstanceId desc, e.jobExecutionId desc",
										ScheduledJobExecution.class).setMaxResults(1).setFirstResult(0);

		query.setParameter("job_name", job.getName());

		List<ScheduledJobExecution> result = query.getResultList();

		if (!result.isEmpty()) {
			return result.get(0);
		}

		return null;
	}

	private ScheduledJob setScheduledJobEnabled(long scheduledJobId, boolean enabled) {
		try {
			TypedQuery<ScheduledJob> query = entityManager.createQuery(
							"select j from scheduled_job j where j.id = :id", ScheduledJob.class).setMaxResults(1);

			query.setParameter("id", scheduledJobId);

			ScheduledJob job = query.getSingleResult();
			job.setEnabled(enabled);

			return job;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public ScheduledJob enable(long scheduledJobId) {
		return this.setScheduledJobEnabled(scheduledJobId, true);
	}

	@Override
	public void disable(long scheduledJobId) {
		this.setScheduledJobEnabled(scheduledJobId, false);
	}

	@Override
	public void schedulePeriodicJob(long scheduledJobId, long period) {
		try {
			TypedQuery<ScheduledJob> query = entityManager.createQuery(
							"select j from scheduled_job j where j.id = :id", ScheduledJob.class).setMaxResults(1);

			query.setParameter("id", scheduledJobId);

			ScheduledJob job = query.getSingleResult();
			job.setPeriod(period);
			job.setCronExpression(null);
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public void scheduleCronJob(long scheduledJobId, String cronExpression) {
		try {
			TypedQuery<ScheduledJob> query = entityManager.createQuery(
							"select j from scheduled_job j where j.id = :id", ScheduledJob.class).setMaxResults(1);

			query.setParameter("id", scheduledJobId);

			ScheduledJob job = query.getSingleResult();
			job.setPeriod(null);
			job.setCronExpression(cronExpression);
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

}
