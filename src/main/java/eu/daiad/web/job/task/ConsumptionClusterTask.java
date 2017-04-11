package eu.daiad.web.job.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.SurveyEntity;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.admin.Counter;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.group.Cluster;
import eu.daiad.web.model.group.Segment;
import eu.daiad.web.model.profile.ComparisonRanking;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.model.query.MeterUserDataPoint;
import eu.daiad.web.model.query.RankingDataPoint;
import eu.daiad.web.model.query.UserDataPoint;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IDeviceRepository;
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
     * Parameter name for setting the reference timestamp.
     */
    private final String PARAMETER_REF_TIMESTAMP = "reference.timestamp";

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
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Repository for updating water IQ data.
     */
    @Autowired
    @Qualifier("jpaWaterIqRepository")
    private IWaterIqRepository waterIqRepository;

    /**
     * Repository for querying smart water meter data stored in HBase.
     */
    @Autowired
    private IDataService dataService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Get cluster name
            String clusterName = (String) parameters.get(EnumInParameter.CLUSTER_NAME.getValue());

            // Get segment names
            String[] segmentNames = StringUtils.split((String) parameters.get(EnumInParameter.SEGMENT_NAMES.getValue()), ";");

            if ((segmentNames == null) || (segmentNames.length == 0)) {
                // The number of segment names must match the number of segments
                throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER)
                        .set("parameter", EnumInParameter.SEGMENT_NAMES.getValue())
                        .set("value", (String) parameters.get(EnumInParameter.SEGMENT_NAMES.getValue()));
            }

            // Get max distance of neighbors
            float maxDistance = Float.parseFloat((String) parameters.get(EnumInParameter.DISTANCE.getValue()));
            if (maxDistance <= 0) {
                // Max distance must be a positive value
                throw createApplicationException(SchedulerErrorCode.SCHEDULER_INVALID_PARAMETER)
                        .set("parameter", EnumInParameter.DISTANCE.getValue())
                        .set("value", (String) parameters.get(EnumInParameter.DISTANCE.getValue()));
            }

            // Delete the existing cluster and its segments and members
            groupRepository.deleteAllClusterByName(clusterName);

            // Delete stale water IQ data
            waterIqRepository.clean(365);

            for (UtilityInfo utility : utilityRepository.getUtilities()) {
                // Collection of user comparison and ranking data
                UserComparisonAndRankingCollection comparisons = new UserComparisonAndRankingCollection(maxDistance);

                // Initialize context
                ExecutionContext context = new ExecutionContext();

                context.utilityKey = utility.getKey();
                context.timezone = DateTimeZone.forID(utility.getTimezone());
                context.clusterName = clusterName;
                context.labels = segmentNames;

                // Get utility counters. The job computes segments only
                // for utilities that have at least a registered user
                // and a smart water meter assigned to him
                Map<String, Counter> counters = utilityRepository.getCounters(utility.getId());

                if ((counters.containsKey(COUNTER_USER)) && (counters.containsKey(COUNTER_METER))) {
                    long totalUsers = counters.get(COUNTER_USER).getValue();
                    long totalMeters = counters.get(COUNTER_METER).getValue();

                    if ((totalUsers > 0) && (totalMeters > 0)) {
                        // Get current date and time that will be used as a
                        // reference point in time for computing all other dates
                        // required by the step execution
                        if (parameters.get(PARAMETER_REF_TIMESTAMP) != null) {
                            context.reference = new DateTime(Long.parseLong((String) parameters.get(PARAMETER_REF_TIMESTAMP)),
                                                             context.timezone);
                        } else {
                            context.reference = new DateTime(context.timezone);
                        }

                        // Get time interval
                        computeDateInterval(context);

                        // Get user ranking
                        computeRanking(context);

                        // Compute user monthly water consumption range
                        computeConsumptionRange(context, comparisons);

                        // Create cluster
                        createCluster(context, comparisons);

                        // Compute comparisons and rankings for all users
                        computeComparisonAndRanking(context, comparisons);
                    }
                }
            }
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED)
                    .set("step", chunkContext.getStepContext().getStepName());
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Computes the time interval.
     *
     * @param context the execution context.
     */
    private void computeDateInterval(ExecutionContext context) {
        context.start = context.reference.withDayOfMonth(1).minusMonths(1);
        context.end = context.start.dayOfMonth().withMaximumValue();

        // Adjust start/end dates
        context.start = new DateTime(context.start.getYear(),
                                     context.start.getMonthOfYear(),
                                     context.start.getDayOfMonth(),
                                     0, 0, 0, context.timezone);

        context.end = new DateTime(context.end.getYear(),
                                   context.end.getMonthOfYear(),
                                   context.end.getDayOfMonth(),
                                   23, 59, 59, context.timezone);
    }

    /**
     * Computes the ranking of users based on monthly consumption.
     *
     * @param context the execution context.
     */
    private void computeRanking(ExecutionContext context) {
        DataQuery query = DataQueryBuilder.create()
                                          .timezone(context.timezone)
                                          .absolute(context.start, context.end, EnumTimeAggregation.ALL)
                                          .utilityTop("Utility", context.utilityKey, EnumMetric.SUM, Integer.MAX_VALUE)
                                          .meter()
                                          .build();

        DataQueryResponse result = dataService.execute(query);

        if ((!result.getMeters().isEmpty()) &&
            (!result.getMeters().get(0).getPoints().isEmpty())) {
            context.ranking = (RankingDataPoint) result.getMeters().get(0).getPoints().get(0);
        }
    }

    /**
     * Computes the user monthly consumption range.
     *
     * @param context the execution context.
     * @comparisons collection of user comparisons and rankings.
     */
    private void computeConsumptionRange(ExecutionContext context, UserComparisonAndRankingCollection comparisons) {
        if(context.ranking == null) {
            return;
        }

        double min = Double.MAX_VALUE;
        double max = 0.0;

        // Compute minimum/maximum consumption and fetch user data
        for (UserDataPoint point : context.ranking.getUsers()) {
            MeterUserDataPoint meter = (MeterUserDataPoint) point;

            // Get survey data and compute consumption per household member
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

            AccountEntity account = userRepository.getAccountByKey(meter.getKey());
            comparisons.addUser(meter.getKey(), householdSize, meter.getVolume().get(EnumMetric.SUM), account.getLocation());
        }

        context.minConsumption = min;
        context.maxConsumption = max;
    }

    /**
     * Group users based on their monthly consumption
     *
     * @param context the execution context.
     * @comparisons collection of user comparisons and rankings.
     */
    private void createCluster(ExecutionContext context, UserComparisonAndRankingCollection comparisons) {
        if(context.ranking == null) {
            return;
        }

        // Create cluster and segments
        Cluster cluster = new Cluster();

        cluster.setName(context.clusterName);
        cluster.setKey(UUID.randomUUID());
        cluster.setSize(context.ranking.getUsers().size());
        cluster.setUtilityKey(context.utilityKey);

        for (String name : context.labels) {
            Segment segment = new Segment();

            segment.setKey(UUID.randomUUID());
            segment.setName(name);
            segment.setUtilityKey(context.utilityKey);

            cluster.getSegments().add(segment);
        }

        // Assign users to segments
        double step = (context.maxConsumption - context.minConsumption) / context.labels.length;
        for (UserDataPoint point : context.ranking.getUsers()) {
            MeterUserDataPoint meter = (MeterUserDataPoint) point;

            int index = (int) ((meter.getVolume().get(EnumMetric.SUM) - context.minConsumption) / step);
            if (index == context.labels.length) {
                index -= 1;
            }

            cluster.getSegments().get(index).getMembers().add(meter.getKey());

            // Override value if savings potential data exist
            List<Device> devices = deviceRepository.getUserDevices(point.getKey(), EnumDeviceType.METER);
            if (devices.size() == 1) {
                index = overrideWaterIqWithSavingPotentialResult(context,
                                                                 index,
                                                                 context.start.getMonthOfYear(),
                                                                 ((WaterMeterDevice) devices.get(0)).getSerial());
            }

            comparisons.getUserByKey(meter.getKey()).getSelf().value = index;
        }

        // Save cluster
        groupRepository.createCluster(cluster);
    }

    /**
     * Creates or updates a new/existing water IQ
     *
     * @param context job execution data.
     * @param data water IQ data.
     */
    private void computeComparisonAndRanking(ExecutionContext context, UserComparisonAndRankingCollection data) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

        String startAsText = context.start.toString(formatter);
        String endAsText = context.end.toString(formatter);

        ComparisonRanking.WaterIq all = convertWaterIq(context, data.getAll());

        for(UUID key : data.getUserKeys()) {
            UserComparisonAndRanking user = data.getUserByKey(key);

            // Build data query for the last month total consumption
            ComparisonRanking.MonthlyConsumtpion monthlyConsumtpion = new ComparisonRanking.MonthlyConsumtpion(context.start.getYear(), context.start.getMonthOfYear());

            DataQuery lastMonthQuery = DataQueryBuilder.create()
                                                       .timezone(context.timezone)
                                                       .absolute(context.start, context.end, EnumTimeAggregation.ALL)
                                                       .user("self", user.getKey())
                                                       .users("similar", user.getSimilarUsers())
                                                       .users("nearest", user.getNearestUsers())
                                                       .utility("utility", context.utilityKey)
                                                       .sum()
                                                       .meter()
                                                       .build();

            DataQueryResponse result = dataService.execute(lastMonthQuery);

            if (result.getSuccess()) {
                for (GroupDataSeries series : result.getMeters()) {
                    switch (series.getLabel()) {
                        case "self":
                            if (!series.getPoints().isEmpty()) {
                                monthlyConsumtpion.user = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "similar":
                            if (!series.getPoints().isEmpty()) {
                                monthlyConsumtpion.similar = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "nearest":
                            if (!series.getPoints().isEmpty()) {
                                monthlyConsumtpion.nearest = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "utility":
                            if (!series.getPoints().isEmpty()) {
                                monthlyConsumtpion.all = ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
                            }
                            break;
                    }
                }
            }

            // Build data query for the last month total daily consumption
            Map<Integer, ComparisonRanking.DailyConsumption> dailyConsumption = new HashMap<Integer, ComparisonRanking.DailyConsumption>();

            DataQuery dailyQuery = DataQueryBuilder.create()
                                                   .timezone(context.timezone)
                                                   .absolute(context.start, context.end, EnumTimeAggregation.DAY)
                                                   .user("self", user.getKey())
                                                   .users("similar", user.getSimilarUsers())
                                                   .users("nearest", user.getNearestUsers())
                                                   .utility("utility", context.utilityKey)
                                                   .sum()
                                                   .meter()
                                                   .build();

            result = dataService.execute(dailyQuery);

            if (result.getSuccess()) {
                for (GroupDataSeries series : result.getMeters()) {
                    switch (series.getLabel()) {
                        case "self":
                            for(DataPoint point : series.getPoints()) {
                                MeterDataPoint meter = (MeterDataPoint) point;

                                getDailyConsumption(context, dailyConsumption, meter).user = meter.getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "similar":
                            for(DataPoint point : series.getPoints()) {
                                MeterDataPoint meter = (MeterDataPoint) point;

                                getDailyConsumption(context, dailyConsumption, meter).similar = meter.getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "nearest":
                            for(DataPoint point : series.getPoints()) {
                                MeterDataPoint meter = (MeterDataPoint) point;

                                getDailyConsumption(context, dailyConsumption, meter).nearest = meter.getVolume().get(EnumMetric.SUM);
                            }
                            break;
                        case "utility":
                            for(DataPoint point : series.getPoints()) {
                                MeterDataPoint meter = (MeterDataPoint) point;

                                getDailyConsumption(context, dailyConsumption, meter).all = meter.getVolume().get(EnumMetric.SUM);
                            }
                            break;
                    }
                }
            }

            waterIqRepository.update(user.getKey(),
                                     startAsText,
                                     endAsText,
                                     convertWaterIq(context, user.getSelf()),
                                     convertWaterIq(context, user.getSimilarWaterIq()),
                                     convertWaterIq(context, user.getNearestWaterIq()),
                                     all,
                                     monthlyConsumtpion,
                                     toList(dailyConsumption.values()));
        }
    }

    /**
     * Helper method for converting a collection of values to a list.
     *
     * @param values the collection of values.
     * @return the new list.
     */
    private <T> List<T> toList(Collection<T> values) {
        if(values == null) {
            return null;
        }
        List<T> result = new ArrayList<T>();
        for(T o : values) {
            result.add(o);
        }
        return result;
    }

    /**
     * Gets or creates a new daily consumption data point.
     *
     * @param context the execution context.
     * @param dailyConsumption a map that holds an instance of {@link ComparisonRanking.DailyConsumption} for every day.
     * @param point the data point to add.
     * @return an existing or new {@link ComparisonRanking.DailyConsumption} object.
     */
    private ComparisonRanking.DailyConsumption getDailyConsumption(ExecutionContext context,
                                                                   Map<Integer, ComparisonRanking.DailyConsumption> dailyConsumption,
                                                                   MeterDataPoint point) {
        DateTime localDateTime = new DateTime(point.getTimestamp(), context.timezone);

        int year = localDateTime.getYear();
        int month = localDateTime.getMonthOfYear();
        int week =localDateTime.getWeekOfWeekyear();
        int day = localDateTime.getDayOfMonth();

        int key = month * 100 + day;

        ComparisonRanking.DailyConsumption consumption = dailyConsumption.get(key);
        if (consumption == null) {
            consumption = new ComparisonRanking.DailyConsumption(year, month, week, day);
            dailyConsumption.put(key, consumption);
        }

        return consumption;
    }

    /**
     * Converts {@link WaterIq} to {@link ComparisonRanking.WaterIq}
     *
     * @param context job execution data.
     * @param waterIq the value to convert.
     * @return the new {@link ComparisonRanking.WaterIq} object.
     */
    private ComparisonRanking.WaterIq convertWaterIq(ExecutionContext context, WaterIq waterIq) {
        ComparisonRanking.WaterIq result = new  ComparisonRanking.WaterIq();

        result.volume = Math.round(waterIq.volume * 100) / 100d;
        if (waterIq.value == context.labels.length) {
            waterIq.value = context.labels.length - 1;
        }
        result.value = context.labels[waterIq.value];

        return result;
    }

    /**
     * A simple store for data required during the job execution
     *
     */
    private static class ExecutionContext {

        /**
         * Utility unique key.
         */
        UUID utilityKey;

        /**
         * Reference date time for computing query time intervals.
         */
        DateTime reference;

        /**
         * Time zone.
         */
        DateTimeZone timezone;

        /**
         * Time interval start date.
         */
        DateTime start;

        /**
         * Time interval end date.
         */
        DateTime end;

        /**
         * The cluster name
         */
        String clusterName;

        /**
         * Segment names used as waterIQ values.
         */
        String[] labels;

        /**
         * User ranking based on monthly consumption.
         */
        RankingDataPoint ranking = null;

        /**
         * Minimum user consumption.
         */
        double minConsumption = Double.MAX_VALUE;

        /**
         * Maximum user consumption
         */
        double maxConsumption = 0;
    }


    /**
     * Enumeration of job input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Cluster name.
         */
        CLUSTER_NAME("cluster.name"),
        /**
         * Segment names separated with (;).
         */
        SEGMENT_NAMES("cluster.segments"),
        /**
         * Distance in meters for computing nearest consumers.
         */
        DISTANCE("nearest.distance"),
        /**
         * Reference timestamp for computing year and month.
         */
        REFERENCE_TIMESTAMP("reference.timestamp");


        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

    private int overrideWaterIqWithSavingPotentialResult(ExecutionContext context, int current, int month, String serial) {
        String value = waterIqRepository.getWaterIqFromSavingsPotential(month, serial);

        if (StringUtils.isBlank(value)) {
            return current;
        }

        for (int i = 0, count = context.labels.length; i < count; i++) {
            if (context.labels[i].equalsIgnoreCase(value)) {
                return i;
            }
        }

        return current;
    }
}
