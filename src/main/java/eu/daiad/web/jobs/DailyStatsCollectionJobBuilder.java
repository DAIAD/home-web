package eu.daiad.web.jobs;

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
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.admin.DailyCounterEntity;
import eu.daiad.web.domain.application.Utility;

@Component
public class DailyStatsCollectionJobBuilder implements IJobBuilder {

    private static final Log logger = LogFactory.getLog(DailyStatsCollectionJobBuilder.class);

    private final String COUNTER_USER = "user";

    private final String COUNTER_METER = "meter";

    private final String COUNTER_AMPHIRO = "amphiro";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @PersistenceContext(unitName = "management")
    private EntityManager adminEntityManager;

    public DailyStatsCollectionJobBuilder() {

    }

    private Step computeStats() {
        return stepBuilderFactory.get("generateMessages").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                try {
                    DateTime today = new DateTime();

                    // Get all utilities
                    TypedQuery<Utility> utilityQuery = entityManager.createQuery("select u from utility u ",
                                    Utility.class);

                    for (Utility utility : utilityQuery.getResultList()) {
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
