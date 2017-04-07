package eu.daiad.web.job.task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.SavingsPotentialResultEntity;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.model.query.savings.SavingScenario;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.ISavingsPotentialRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.savings.ISavingsPotentialService;

/**
 * Task for computing savings potential per user.
 */
@Component
public class SavingsPotentialComputeTask extends BaseTask implements StoppableTasklet {

    /**
     * Service for accessing savings potential scenario data.
     */
    @Autowired
    private ISavingsPotentialService savingsPotentialService;

    /**
     * Repository for updating savings potential scenario data.
     */
    @Autowired
    private ISavingsPotentialRepository savingsPotentialRepository;

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
     * Service for querying user consumption data.
     */
    @Autowired
    private IDataService dataService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Get scenario
            if(StringUtils.isBlank(parameters.get(EnumInParameter.SCENARIO_KEY.getValue()))) {
                return RepeatStatus.FINISHED;
            }

            UUID scenarioKey = UUID.fromString(parameters.get(EnumInParameter.SCENARIO_KEY.getValue()));
            SavingScenario scenario = savingsPotentialService.find(scenarioKey);

            // Get utility
            UtilityInfo utility = utilityRepository.getUtilityByKey(scenario.getUtilityKey());

            // Get scenario results
            SavingResultStore resultStore = new SavingResultStore();
            for(SavingsPotentialResultEntity result : savingsPotentialService.getScenarioResults(scenario.getKey())) {
                resultStore.add(result.getMonth(), result.getSerial(), result.getSavingsVolume() / (double) result.getClusterSize());
            }

            // Compute date interval
            DateTimeZone timezone = DateTimeZone.forID(utility.getTimezone());

            DateTime begin = new DateTime(scenario.getParameters().getTime().getStart(), timezone);
            begin = begin.dayOfMonth().withMinimumValue()
                         .hourOfDay().setCopy(0).minuteOfHour().setCopy(0).secondOfMinute().setCopy(0).millisOfSecond().setCopy(0);

            DateTime end = new DateTime(scenario.getParameters().getTime().getEnd(), timezone);
            end = end.dayOfMonth().withMaximumValue()
                     .hourOfDay().setCopy(23).minuteOfHour().setCopy(59).secondOfMinute().setCopy(59).millisOfSecond().setCopy(999);

            // Compute savings per user
            double scenarioConsumption = 0;
            double scenarioSavings = 0;

            Map<String, UUID> serialToUserMap = new HashMap<String, UUID>();
            Map<UUID, AccountSavings> savingsStore = new HashMap<UUID, AccountSavings>();

            while(begin.isBefore(end)) {
                int year = begin.getYear();
                int month = begin.getMonthOfYear();

                Map<String, SavingResult> monthlyResults = resultStore.results.get(month);

                if(monthlyResults == null) {
                    begin = begin.plusMonths(1);
                    continue;
                }

                for (SavingResult consumer : monthlyResults.values()) {
                    // Resolve user key and cache it
                    UUID userKey = serialToUserMap.get(consumer.serial);
                    if(userKey == null) {
                        AccountEntity account = userRepository.getUserByMeterSerial(consumer.serial);
                        if(account == null) {
                            continue;
                        }
                        userKey = account.getKey();
                        serialToUserMap.put(consumer.serial, userKey);
                    }

                    DataQuery query = DataQueryBuilder.create()
                                                      .timezone(timezone)
                                                      .absolute(begin, begin.dayOfMonth().withMaximumValue(), EnumTimeAggregation.MONTH)
                                                      .user(consumer.serial, userKey)
                                                      .meter()
                                                      .userAggregates()
                                                      .sum()
                                                      .build();
                    DataQueryResponse result = dataService.execute(query);
                    if(result.getMeters().isEmpty()) {
                        continue;
                    }

                    for (DataPoint point : result.getMeters().get(0).getPoints()) {
                        MeterDataPoint meterPoint = (MeterDataPoint) point;
                        DateTime instant = new DateTime(meterPoint.getTimestamp(), timezone);

                        if ((instant.getYear() == year) && (instant.getMonthOfYear() == month)) {
                            AccountSavings s = savingsStore.get(userKey);
                            if (s == null) {
                                s = new AccountSavings();
                                s.userKey = userKey;
                                savingsStore.put(userKey, s);
                            }

                            s.totalConsumption += meterPoint.getVolume().get(EnumMetric.SUM);
                            s.totalSavings += consumer.userMonthlyPotential;

                            scenarioConsumption += meterPoint.getVolume().get(EnumMetric.SUM);
                            scenarioSavings += consumer.userMonthlyPotential;

                            continue;
                        }
                    }
                }

                begin = begin.plusMonths(1);
            }

            // Update store
            for (AccountSavings s : savingsStore.values()) {
                savingsPotentialRepository.updateSavings(scenarioKey,
                                                         s.userKey,
                                                         s.totalConsumption,
                                                         s.totalSavings,
                                                         DateTime.now());
            }

            savingsPotentialRepository.updateSavings(scenarioKey,
                                                     scenarioConsumption,
                                                     scenarioSavings,
                                                     DateTime.now());

        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED).set("step", chunkContext.getStepContext().getStepName());
        }

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Enumeration of job input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Savings potential scenario key.
         */
        SCENARIO_KEY("scenario.key");


        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

    private static class SavingResultStore {

        public Map<Integer, Map<String, SavingResult>> results = new HashMap<Integer, Map<String, SavingResult>>();

        public void add(int month, String serial, double userMonthlyPotential) {
            Map<String, SavingResult> monthlySavings = results.get(month);
            if (monthlySavings == null) {
                monthlySavings = new HashMap<String, SavingResult>();
                results.put(month, monthlySavings);
            }
            monthlySavings.put(serial, new SavingResult(serial, userMonthlyPotential));
        }
    }

    private static class SavingResult {

        public String serial;

        public double userMonthlyPotential;

        public SavingResult(String serial, double userMonthlyPotential) {
            this.serial = serial;
            this.userMonthlyPotential = userMonthlyPotential;
        }
    }

    private static class AccountSavings {

        public UUID userKey;

        public double totalConsumption = 0;

        public double totalSavings = 0;
    }
}
