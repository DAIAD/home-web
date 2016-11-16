package eu.daiad.web.job.task;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.SurveyEntity;
import eu.daiad.web.model.admin.Counter;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.group.Cluster;
import eu.daiad.web.model.group.Segment;
import eu.daiad.web.model.profile.ComparisonRanking;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumTimeAggregation;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.model.query.MeterUserDataPoint;
import eu.daiad.web.model.query.RankingDataPoint;
import eu.daiad.web.model.query.UserDataPoint;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.repository.application.IWaterIqRepository;
import eu.daiad.web.service.IDataService;

/**
 * Task that clusters users based on their consumption and computes water IQ status.
 */
@Component
public class ConsumptionClusterTask extends BaseTask implements StoppableTasklet {

    /**
     * Name of the step that computes the clusters
     */
    private final String TASK_CLUSTER_CREATION = "createClusterSegments";

    /**
     * Parameter name for the unique cluster name.
     */
    private final String PARAMETER_CLUSTER_NAME = "cluster.name";

    /**
     * Parameter name for a semicolon delimited list of segment unique names.
     * The size of the list must match the value of PARAMETER_CLUSTER_SIZE
     * parameter.
     */
    private final String PARAMETER_SEGMENT_NAMES = "cluster.segments";

    /**
     * Parameter name for the distance of the nearest neighbors.
     */
    private final String PARAMETER_NEAREST_DISTANCE = "nearest.distance";

    /**
     * User counter name.
     */
    private final String COUNTER_USER = "user";

    /**
     * Meter counter name.
     */
    private final String COUNTER_METER = "meter";

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing group data.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for updating water IQ data.
     */
    @Autowired
    private IWaterIqRepository waterIqRepository;

    /**
     * Repository for querying smart water meter data stored in HBase.
     */
    @Autowired
    private IDataService dataService;


    /**
     * The step name.
     *
     * @return the step name.
     */
    @Override
    public  String getName() {
        return TASK_CLUSTER_CREATION;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();

            // Get cluster name
            String clusterName = (String) jobParameters.get(PARAMETER_CLUSTER_NAME);

            // Get segment names
            String[] names = StringUtils.split((String) jobParameters.get(PARAMETER_SEGMENT_NAMES), ";");

            if ((names == null) || (names.length == 0)) {
                // The number of segment names must match the number of segments
                throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER)
                        .set("parameter", PARAMETER_SEGMENT_NAMES)
                        .set("value", (String) jobParameters.get(PARAMETER_SEGMENT_NAMES));
            }

            // Get max distance of neighbors
            float maxDistance = Float.parseFloat((String) jobParameters.get(PARAMETER_NEAREST_DISTANCE));
            if (maxDistance <= 0) {
                // Max distance must be a positive value
                throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER)
                        .set("parameter", PARAMETER_NEAREST_DISTANCE)
                        .set("value", (String) jobParameters.get(PARAMETER_NEAREST_DISTANCE));
            }

            // Delete the existing cluster and its segments and members
            groupRepository.deleteAllClusterByName(clusterName);

            // Delete stale water IQ data
            waterIqRepository.clean(60);

            for (UtilityInfo utility : utilityRepository.getUtilities()) {
                // Water IQ data
                UserWaterIqCollection waterIq = new UserWaterIqCollection(utility.getKey(), maxDistance);

                // Get default time zone
                DateTimeZone timezone = DateTimeZone.forID(utility.getTimezone());

                // Get utility counters. The job computes segments only
                // for utilities that have at least a registered user
                // and a smart water meter assigned to him
                Map<String, Counter> counters = utilityRepository.getCounters(utility.getId());

                if ((counters.containsKey(COUNTER_USER)) && (counters.containsKey(COUNTER_METER))) {
                    long totalUsers = counters.get(COUNTER_USER).getValue();
                    long totalMeters = counters.get(COUNTER_METER).getValue();

                    if ((totalUsers > 0) && (totalMeters > 0)) {
                        // Find last Sunday before today
                        DateTime now = new DateTime(timezone);

                        int offset = (now.getDayOfWeek() ) % 7;

                        DateTime end = now.minusDays(offset);

                        // Get last Monday before today
                        DateTime start = end.minusDays(6);

                        // Adjust start/end dates
                        start = new  DateTime(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), 0, 0, 0, timezone);
                        end = new  DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), 23, 59, 59, timezone);

                        // Build data query
                        DataQuery query = DataQueryBuilder.create()
                                                          .timezone(timezone)
                                                          .absolute(start, end, EnumTimeAggregation.ALL)
                                                          .utilityTop(utility.getName(), utility.getKey(), EnumMetric.SUM, (int) totalMeters)
                                                          .meter()
                                                          .build();

                        DataQueryResponse result = dataService.execute(query);

                        if (!result.getMeters().isEmpty()) {
                            GroupDataSeries series = result.getMeters().get(0);

                            double min = Double.MAX_VALUE;
                            double max = 0.0;

                            if (!series.getPoints().isEmpty()) {
                                RankingDataPoint ranking = (RankingDataPoint) series.getPoints().get(0);

                                // Compute minimum/maximum consumption and fetch user data
                                for (UserDataPoint user : ranking.getUsers()) {
                                    MeterUserDataPoint meter = (MeterUserDataPoint) user;

                                    // Get survey data and compute consumption per household member
                                    AccountEntity account = userRepository.getAccountByKey(meter.getKey());
                                    SurveyEntity survey = userRepository.getSurveyByKey(meter.getKey());

                                    int householdSize = 1;
                                    double volume = meter.getVolume().get(EnumMetric.SUM);

                                    if ((survey != null) && (survey.getHouseholdMemberTotal() > 0)) {
                                        householdSize = survey.getHouseholdMemberTotal();
                                    }
                                    volume /= householdSize;

                                    meter.getVolume().put(EnumMetric.SUM, volume);

                                    // Adjust minimum/maximum consumption values
                                    if (min > volume) {
                                        min = volume;
                                    }
                                    if (max < volume) {
                                        max = volume;
                                    }

                                    waterIq.addUser(meter.getKey(), householdSize, meter.getVolume().get(EnumMetric.SUM), account.getLocation());
                                }

                                if (min < max) {
                                    // Create cluster and segments
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

                                    // Assign users to segments
                                    double step = (max - min) / names.length;
                                    for (UserDataPoint user : ranking.getUsers()) {
                                        MeterUserDataPoint meter = (MeterUserDataPoint) user;

                                        int index = (int) ((meter.getVolume().get(EnumMetric.SUM) - min) / step);
                                        if (index == names.length) {
                                            index -= 1;
                                        }

                                        cluster.getSegments().get(index).getMembers().add(meter.getKey());

                                        waterIq.getUsers().get(meter.getKey()).getSelf().value = index;
                                    }

                                    groupRepository.createCluster(cluster);

                                    // Update water IQ
                                    updateWaterIq(now, timezone, start, end, names, waterIq);
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
                    .set("step", TASK_CLUSTER_CREATION);
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Creates or updates a new/existing water IQ
     *
     * @param reference reference date time for computing query time intervals.
     * @param timezone time zone.
     * @param start date interval start.
     * @param end date interval end.
     * @param labels segments names.
     * @param data water IQ data.
     */
    private void updateWaterIq(DateTime reference, DateTimeZone timezone, DateTime start, DateTime end, String[] labels, UserWaterIqCollection data) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

        String startAsText = start.toString(formatter);
        String endAsText = end.toString(formatter);

        ComparisonRanking.WaterIq all = convertWaterIq(labels, data.getAll());

        for(UserWaterIq user : data.getUsers().values()) {

            // Build data query for last month consumption
            DataQuery last1MonthQuery = DataQueryBuilder.create()
                                                        .timezone(timezone)
                                                        .absolute(reference.minusDays(30), reference, EnumTimeAggregation.ALL)
                                                        .user("self", user.getKey())
                                                        .users("similar", user.getSimilarUsers())
                                                        .users("nearest", user.getNearestUsers())
                                                        .utility("utility", data.getUtilityKey())
                                                        .sum()
                                                        .meter()
                                                        .build();

            DataQueryResponse result = dataService.execute(last1MonthQuery);

            Double self1Month = null, similar1Month = null, nearest1Month = null, utility1Month = null;
            if (result.getSuccess()) {
                for (GroupDataSeries series : result.getMeters()) {
                    switch (series.getLabel()) {
                        case "self":
                            if (!series.getPoints().isEmpty()) {
                                self1Month = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "similar":
                            if (!series.getPoints().isEmpty()) {
                                similar1Month = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "nearest":
                            if (!series.getPoints().isEmpty()) {
                                nearest1Month = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "utility":
                            if (!series.getPoints().isEmpty()) {
                                utility1Month = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                    }
                }
            }

            // Build data query for last 6 month consumption
            DataQuery last6MonthQuery = DataQueryBuilder.create()
                                                        .timezone(timezone)
                                                        .absolute(reference.minusDays(180), reference, EnumTimeAggregation.ALL)
                                                        .user("self", user.getKey())
                                                        .users("similar", user.getSimilarUsers())
                                                        .users("nearest", user.getNearestUsers())
                                                        .utility("utility", data.getUtilityKey())
                                                        .sum()
                                                        .meter()
                                                        .build();

            result = dataService.execute(last6MonthQuery);

            Double self6Month = null, similar6Month = null, nearest6Month = null, utility6Month = null;
            if (result.getSuccess()) {
                for (GroupDataSeries series : result.getMeters()) {
                    switch (series.getLabel()) {
                        case "self":
                            if (!series.getPoints().isEmpty()) {
                                self6Month = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "similar":
                            if (!series.getPoints().isEmpty()) {
                                similar6Month = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "nearest":
                            if (!series.getPoints().isEmpty()) {
                                nearest6Month = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "utility":
                            if (!series.getPoints().isEmpty()) {
                                utility6Month = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                    }
                }
            }

            waterIqRepository.update(user.getKey(),
                                     startAsText,
                                     endAsText,
                                     convertWaterIq(labels, user.getSelf()),
                                     convertWaterIq(labels, user.getSimilarWaterIq()),
                                     convertWaterIq(labels, user.getNearestWaterIq()),
                                     all,
                                     new ComparisonRanking.MonthlyConsumtpion(self1Month, similar1Month, nearest1Month, utility1Month),
                                     new ComparisonRanking.MonthlyConsumtpion(self6Month, similar6Month, nearest6Month, utility6Month));
        }
    }

    /**
     * Converts {@link WaterIq} to {@link ComparisonRanking.WaterIq}
     *
     * @param labels segment labels.
     * @param waterIq the value to convert.
     * @return the new {@link ComparisonRanking.WaterIq} object.
     */
    private ComparisonRanking.WaterIq convertWaterIq(String[] labels, WaterIq waterIq) {
        ComparisonRanking.WaterIq result = new  ComparisonRanking.WaterIq();

        result.volume = Math.round(waterIq.volume * 100) / 100d;
        if (waterIq.value == labels.length) {
            waterIq.value = labels.length - 1;
        }
        result.value = labels[waterIq.value];

        return result;
    }

}
