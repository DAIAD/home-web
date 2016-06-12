package eu.daiad.web.jobs;

import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.admin.Counter;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.group.Cluster;
import eu.daiad.web.model.group.Segment;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumTimeAggregation;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterUserDataPoint;
import eu.daiad.web.model.query.RankingDataPoint;
import eu.daiad.web.model.query.UserDataPoint;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.IDataService;

@Component
public class ConsumptionClusterJobBuilder extends BaseJobBuilder implements IJobBuilder {

    private final String STEP_NAME = "createClusterSegments";

    private final String PARAMETER_CLUSTER_NAME = "cluster.name";

    private final String PARAMETER_CLUSTER_SIZE = "cluster.size";

    private final String PARAMETER_SEGMENT_NAMES = "cluster.segments";

    private final String PARAMETER_START = "date.start";

    private final String PARAMETER_END = "date.end";

    private final String PARAMETER_PATTERN = "date.pattern";

    private final String COUNTER_USER = "user";

    private final String COUNTER_METER = "meter";

    @Value("${daiad.batch.server-time-zone:Europe/Athens}")
    private String timezone;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private IUtilityRepository utilityRepository;

    @Autowired
    private IGroupRepository groupRepository;

    @Autowired
    private IDataService dataService;

    @Autowired
    @Qualifier("applicationDataSource")
    private DataSource dataSource;

    private Step createClusterSegments() {

        return stepBuilderFactory.get(STEP_NAME).tasklet(new StoppableTasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                try {
                    String value;

                    String clusterName = (String) chunkContext.getStepContext().getJobParameters().get(
                                    PARAMETER_CLUSTER_NAME);

                    value = (String) chunkContext.getStepContext().getJobParameters().get(PARAMETER_CLUSTER_SIZE);
                    int clusterSize = Integer.parseInt(value);

                    groupRepository.deleteAllClusterByName(clusterName);

                    if (clusterSize <= 0) {
                        throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER).set(
                                        "parameter", PARAMETER_CLUSTER_SIZE).set("value", value);
                    }

                    value = (String) chunkContext.getStepContext().getJobParameters().get(PARAMETER_SEGMENT_NAMES);
                    String[] names = StringUtils.split(value, ";");

                    if (names.length != clusterSize) {
                        throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER).set(
                                        "parameter", PARAMETER_SEGMENT_NAMES).set("value", value);
                    }

                    for (UtilityInfo utility : utilityRepository.getUtilities()) {
                        Map<String, Counter> counters = utilityRepository.getCounters(utility.getId());

                        if ((counters.containsKey(COUNTER_USER)) && (counters.containsKey(COUNTER_METER))) {
                            long totalUsers = counters.get(COUNTER_USER).getValue();
                            long totalMeters = counters.get(COUNTER_METER).getValue();

                            if ((totalUsers > 0) && (totalMeters > 0)) {
                                String parameter, pattern;

                                pattern = (String) chunkContext.getStepContext().getJobParameters().get(
                                                PARAMETER_PATTERN);

                                DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern).withZone(
                                                DateTimeZone.forID(timezone));

                                DateTime start = new DateTime();
                                parameter = (String) chunkContext.getStepContext().getJobParameters().get(
                                                PARAMETER_START);

                                if (!StringUtils.isBlank(parameter)) {
                                    start = formatter.parseDateTime(parameter);
                                }

                                DateTime end = start.minusDays(30);
                                parameter = (String) chunkContext.getStepContext().getJobParameters()
                                                .get(PARAMETER_END);

                                if (!StringUtils.isBlank(parameter)) {
                                    end = formatter.parseDateTime(parameter);
                                }

                                DataQuery query = DataQueryBuilder.create().absolute(start, end,
                                                EnumTimeAggregation.ALL).utilityTop(utility.getName(),
                                                utility.getKey(), EnumMetric.SUM, (int) totalMeters).build();

                                DataQueryResponse result = dataService.execute(query);

                                if (!result.getMeters().isEmpty()) {
                                    GroupDataSeries series = result.getMeters().get(0);

                                    double min = 0.0;
                                    double max = 0.0;

                                    if (!series.getPoints().isEmpty()) {
                                        RankingDataPoint ranking = (RankingDataPoint) series.getPoints().get(0);

                                        for (UserDataPoint user : ranking.getUsers()) {
                                            MeterUserDataPoint meter = (MeterUserDataPoint) user;
                                            if (min > meter.getVolume().get(EnumMetric.SUM)) {
                                                min = meter.getVolume().get(EnumMetric.SUM);
                                            }
                                            if (max < meter.getVolume().get(EnumMetric.SUM)) {
                                                max = meter.getVolume().get(EnumMetric.SUM);
                                            }
                                        }

                                        if (min < max) {
                                            Cluster cluster = new Cluster();

                                            cluster.setName(clusterName);
                                            cluster.setKey(UUID.randomUUID());
                                            cluster.setSize(ranking.getUsers().size());
                                            cluster.setUtilityKey(utility.getKey());

                                            for (String name : names) {
                                                Segment segment = new Segment();

                                                segment.setKey(UUID.randomUUID());
                                                segment.setName(name);
                                                segment.setUtilityKey(utility.getKey());

                                                cluster.getSegments().add(segment);
                                            }

                                            double step = (max - min) / clusterSize;
                                            for (UserDataPoint user : ranking.getUsers()) {
                                                MeterUserDataPoint meter = (MeterUserDataPoint) user;

                                                int index = (int) ((meter.getVolume().get(EnumMetric.SUM) - min) / step);
                                                if (index == clusterSize) {
                                                    index -= 1;
                                                }

                                                cluster.getSegments().get(index).getMembers().add(meter.getKey());
                                            }

                                            groupRepository.createCluster(cluster);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (ApplicationException ex) {
                    throw ex;
                } catch (Throwable t) {
                    throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAIL)
                                    .set("step", STEP_NAME);
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
        return jobBuilderFactory.get(name).incrementer(incrementer).start(createClusterSegments()).build();
    }
}
