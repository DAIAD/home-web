package eu.daiad.scheduler.job.builder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import eu.daiad.common.domain.admin.DailyCounterEntity;
import eu.daiad.common.domain.application.UtilityEntity;

/**
 * Helper builder class for initializing a job that computes daily statistics.
 */
@Component
public class DailyStatsCollectionJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DailyStatsCollectionJobBuilder.class);

    /**
     * Name of the step that computes daily statistics.
     */
    private final String TASK_COMPUTE_STATISTICS = "compute-daily-statistics";

    /**
     * User counter name.
     */
    private final String COUNTER_USER = "user";

    /**
     * Smart water meter counter name.
     */
    private final String COUNTER_METER = "meter";

    /**
     * Amphiro b1 counter name.
     */
    private final String COUNTER_AMPHIRO = "amphiro";

    /**
     * Entity manager for the application database.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Entity manager for the administration database.
     */
    @PersistenceContext
    private EntityManager adminEntityManager;

    private Step computeStats() {
        return stepBuilderFactory.get(TASK_COMPUTE_STATISTICS).tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                try {
                    DateTime today = new DateTime();

                    // Get all utilities
                    TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery("select u from utility u ",
                                    UtilityEntity.class);

                    for (UtilityEntity utility : utilityQuery.getResultList()) {
                        // Get total users
                        Long totalUsers;

                        TypedQuery<Number> userCountQuery = entityManager.createQuery(
                                        "select count(a.id) from account a where a.utility.id = :utility_id ",
                                        Number.class);

                        userCountQuery.setParameter("utility_id", utility.getId());

                        totalUsers = ((Number) userCountQuery.getSingleResult()).longValue();

                        DailyCounterEntity counter = new DailyCounterEntity();

                        counter.setCreatedOn(today);
                        counter.setName(COUNTER_USER);
                        counter.setValue(totalUsers);
                        counter.setUtilityId(utility.getId());

                        adminEntityManager.persist(counter);

                        // Get meters
                        Long totalMeters;

                        TypedQuery<Number> meterCountQuery = entityManager.createQuery(
                                        "select count(d.id) from device_meter d where d.account.utility.id = :utility_id ",
                                        Number.class);

                        meterCountQuery.setParameter("utility_id", utility.getId());

                        totalMeters = ((Number) meterCountQuery.getSingleResult()).longValue();

                        counter = new DailyCounterEntity();

                        counter.setCreatedOn(today);
                        counter.setName(COUNTER_METER);
                        counter.setValue(totalMeters);
                        counter.setUtilityId(utility.getId());

                        adminEntityManager.persist(counter);

                        // Get Amphiro B1 devices
                        Long totalAmphiroDevices;

                        TypedQuery<Number> amphiroCountQuery = entityManager.createQuery(
                                        "select count(d.id) from device_amphiro d where d.account.utility.id = :utility_id ",
                                        Number.class);

                        amphiroCountQuery.setParameter("utility_id", utility.getId());

                        totalAmphiroDevices = ((Number) amphiroCountQuery.getSingleResult()).longValue();

                        counter = new DailyCounterEntity();

                        counter.setCreatedOn(today);
                        counter.setName(COUNTER_AMPHIRO);
                        counter.setValue(totalAmphiroDevices);
                        counter.setUtilityId(utility.getId());

                        adminEntityManager.persist(counter);
                    }
                } catch (Exception ex) {
                    logger.fatal("Failed to compute daily stats.", ex);

                    throw ex;
                }
                return RepeatStatus.FINISHED;
            }

            @Override
            public void stop() {
                // TODO: Add business logic for stopping processing
            }

        }).build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name).incrementer(incrementer).start(computeStats()).build();
    }
}
