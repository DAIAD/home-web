package eu.daiad.web.service.scheduling;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.admin.ScheduledJob;
import eu.daiad.web.domain.admin.ScheduledJobExecution;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.scheduling.EnumExecutionExitCode;
import eu.daiad.web.model.scheduling.ExecutionQuery;
import eu.daiad.web.model.scheduling.ExecutionQueryResult;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("managementTransactionManager")
public class SchedulerRepository extends BaseRepository implements ISchedulerRepository {

    @PersistenceContext(unitName = "management")
    EntityManager entityManager;

    @Value("${spring.batch.table-prefix}")
    private String batchSchemaPrefix;

    @Autowired
    @Qualifier("managementDataSource")
    private DataSource dataSource;

    @Override
    public List<ScheduledJob> getJobs() {
        try {
            TypedQuery<ScheduledJob> query = entityManager.createQuery("select j from scheduled_job j order by name",
                            ScheduledJob.class);

            return query.getResultList();
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
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
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public ScheduledJob getJobByName(String jobName) {
        try {
            TypedQuery<ScheduledJob> query = entityManager.createQuery(
                            "select j from scheduled_job j where j.name= :jobName", ScheduledJob.class);

            query.setParameter("jobName", jobName);

            List<ScheduledJob> result = query.getResultList();

            if (!result.isEmpty()) {
                return result.get(0);
            }

            return null;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public List<ScheduledJobExecution> getExecutions(String jobName, int startPosition, int maxResult) {
        TypedQuery<ScheduledJobExecution> query = entityManager
                        .createQuery("select e from scheduled_job_execution e "
                                        + "where e.jobName = :jobName order by e.jobInstanceId desc, e.jobExecutionId desc",
                                        ScheduledJobExecution.class).setMaxResults(maxResult).setFirstResult(
                                        startPosition);

        query.setParameter("jobName", jobName);

        return query.getResultList();
    }

    @Override
    public String getExecutionMessage(long executionId) {
        TypedQuery<ScheduledJobExecution> query = entityManager.createQuery(
                        "select e from scheduled_job_execution e where e.job_execution_id = :job_execution_id",
                        ScheduledJobExecution.class).setMaxResults(1);

        query.setParameter("job_execution_id", executionId);

        List<ScheduledJobExecution> executions = query.getResultList();

        if (executions.isEmpty()) {
            return null;
        }
        return executions.get(0).getExitMessage();
    }

    @Override
    public ExecutionQueryResult getExecutions(ExecutionQuery query) {
        ExecutionQueryResult result = new ExecutionQueryResult();

        // Load data
        String command = "";

        try {
            // Resolve filters
            List<String> filters = new ArrayList<String>();

            if (!StringUtils.isBlank(query.getJobName())) {
                filters.add("e.jobName = :jobName");
            }
            if (query.getStartDate() != null) {
                filters.add("e.startedOn >= :start_date");
            }
            if (query.getEndDate() != null) {
                filters.add("e.startedOn <= :end_date");
            }
            if (!query.getExitCode().equals(EnumExecutionExitCode.UNDEFINED)) {
                filters.add("e.exitCode = :exit_code");
            }

            // Count total number of records
            Integer totalEvents;

            command = "select count(e.id) from scheduled_job_execution e ";
            if (!filters.isEmpty()) {
                command += "where " + StringUtils.join(filters, " and ");
            }

            TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);

            if (!StringUtils.isBlank(query.getJobName())) {
                countQuery.setParameter("jobName", query.getJobName());
            }
            if (query.getStartDate() != null) {
                filters.add("e.startedOn >= :start_date");
                countQuery.setParameter("start_date", new DateTime(query.getStartDate()));
            }
            if (query.getEndDate() != null) {
                countQuery.setParameter("end_date", new DateTime(query.getEndDate()));
            }
            if (!query.getExitCode().equals(EnumExecutionExitCode.UNDEFINED)) {
                countQuery.setParameter("exit_code", query.getExitCode());
            }

            totalEvents = ((Number) countQuery.getSingleResult()).intValue();

            result.setTotal(totalEvents);

            // Load data
            command = "select e from scheduled_job_execution e ";
            if (!filters.isEmpty()) {
                command += "where " + StringUtils.join(filters, " and ");
            }
            command += " order by e.startedOn desc, e.jobExecutionId desc ";

            TypedQuery<ScheduledJobExecution> entityQuery = entityManager.createQuery(command,
                            ScheduledJobExecution.class);

            if (!StringUtils.isBlank(query.getJobName())) {
                entityQuery.setParameter("jobName", query.getJobName());
            }
            if (query.getStartDate() != null) {
                filters.add("e.startedOn >= :start_date");
                entityQuery.setParameter("start_date", new DateTime(query.getStartDate()));
            }
            if (query.getEndDate() != null) {
                entityQuery.setParameter("end_date", new DateTime(query.getEndDate()));
            }
            if (!query.getExitCode().equals(EnumExecutionExitCode.UNDEFINED)) {
                entityQuery.setParameter("exit_code", query.getExitCode());
            }

            entityQuery.setFirstResult(query.getIndex() * query.getSize());
            entityQuery.setMaxResults(query.getSize());

            result.setExecutions(entityQuery.getResultList());

            return result;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public List<ScheduledJobExecution> getExecutions(long jobId, int startPosition, int size) {
        ScheduledJob job = this.getJobById(jobId);

        TypedQuery<ScheduledJobExecution> query = entityManager.createQuery("select e from scheduled_job_execution e "
                        + "where e.jobName = :jobName order by e.jobInstanceId desc, e.jobExecutionId desc",
                        ScheduledJobExecution.class);

        query.setParameter("jobName", job.getName());

        return query.getResultList();
    }

    @Override
    public ScheduledJobExecution getLastExecution(String jobName) {
        TypedQuery<ScheduledJobExecution> query = entityManager
                        .createQuery("select e from scheduled_job_execution e "
                                        + "where e.jobName = :jobName order by e.jobInstanceId desc, e.jobExecutionId desc",
                                        ScheduledJobExecution.class).setMaxResults(1).setFirstResult(0);

        query.setParameter("jobName", jobName);

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
                                        + "where e.jobName = :jobName order by e.jobInstanceId desc, e.jobExecutionId desc",
                                        ScheduledJobExecution.class).setMaxResults(1).setFirstResult(0);

        query.setParameter("jobName", job.getName());

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
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
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
        TypedQuery<ScheduledJob> query = entityManager.createQuery("select j from scheduled_job j where j.id = :id",
                        ScheduledJob.class).setMaxResults(1);

        query.setParameter("id", scheduledJobId);

        ScheduledJob job = query.getSingleResult();
        job.setPeriod(period);
        job.setCronExpression(null);
    }

    @Override
    public void scheduleCronJob(long scheduledJobId, String cronExpression) {
        TypedQuery<ScheduledJob> query = entityManager.createQuery("select j from scheduled_job j where j.id = :id",
                        ScheduledJob.class).setMaxResults(1);

        query.setParameter("id", scheduledJobId);

        ScheduledJob job = query.getSingleResult();
        job.setPeriod(null);
        job.setCronExpression(cronExpression);
    }

    @Override
    public List<ScheduledJobExecution> getExecutionByExitStatus(ExitStatus exitStatus) {
        TypedQuery<ScheduledJobExecution> query = entityManager.createQuery("select e from scheduled_job_execution e "
                        + "where e.exitCode = :exit_code", ScheduledJobExecution.class);

        query.setParameter("exit_code", EnumExecutionExitCode.fromExistStatus(exitStatus));

        return query.getResultList();
    }

    @Override
    public int updateJobExecutionStatus(long jobExecutionId, BatchStatus status) {
        String command = "UPDATE %PREFIX%JOB_EXECUTION "
                        + "set END_TIME = ?, STATUS = ?, VERSION = VERSION + 1, LAST_UPDATED = ? "
                        + "where JOB_EXECUTION_ID = ?";

        command = StringUtils.replace(command, "%PREFIX%", batchSchemaPrefix);

        Object[] parameters = new Object[] { new Date(System.currentTimeMillis()), status.toString(),
                        new Date(System.currentTimeMillis()), jobExecutionId };

        int[] types = new int[] { Types.TIMESTAMP, Types.VARCHAR, Types.TIMESTAMP, Types.BIGINT };
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        return jdbcTemplate.update(command, parameters, types);
    }
}
