package eu.daiad.web.service.message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountTipEntity;
import eu.daiad.web.domain.application.AlertResolverExecutionEntity;
import eu.daiad.web.domain.application.RecommendationResolverExecutionEntity;
import eu.daiad.web.domain.application.TipEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.EnumDayOfWeek;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.EnumMessageLevel;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.Recommendation;
import eu.daiad.web.model.query.EnumMeasurementField;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IAccountAlertRepository;
import eu.daiad.web.repository.application.IAccountRecommendationRepository;
import eu.daiad.web.repository.application.IAccountTipRepository;
import eu.daiad.web.repository.application.IAlertResolverExecutionRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IRecommendationResolverExecutionRepository;
import eu.daiad.web.repository.application.ITipRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.IConsumptionStatisticsService;
import eu.daiad.web.service.IUtilityConsumptionStatisticsService;
import eu.daiad.web.util.Counters;

@Service
public class DefaultMessageGeneratorService
    implements IMessageGeneratorService
{
    private static final Log logger = LogFactory.getLog(DefaultMessageGeneratorService.class);

    @Value("${daiad.message-generator.tips.enabled:true}")
    private Boolean tipsEnabled;

    @Value("${daiad.message-generator.alerts.enabled:true}")
    private Boolean alertsEnabled;

    @Value("${daiad.message-generator.recommendations.enabled:true}")
    private Boolean recommendationsEnabled;

    private List<String> alertsExcludedResolvers = Collections.emptyList();

    private List<String> recommendationsExcludedResolvers = Collections.emptyList();

    @Value("${daiad.message-generator.alerts.exclude-resolvers:}")
    private void excludeAlertResolvers(String value)
    {
        if (!value.isEmpty())
            alertsExcludedResolvers = Arrays.asList(value.split(",[ ]*"));
    }

    @Value("${daiad.message-generator.recommendations.exclude-resolvers:}")
    private void excludeRecommendationResolvers(String value)
    {
        if (!value.isEmpty())
            recommendationsExcludedResolvers = Arrays.asList(value.split(",[ ]*"));
    }

	@Autowired
	private ApplicationContext context;

	@Autowired
    @Qualifier("defaultBeanValidator")
    private Validator validator;

    @Autowired
	private IUtilityRepository utilityRepository;

    @Autowired
    private IDeviceRepository deviceRepository;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IGroupRepository groupRepository;

	@Autowired
	@Qualifier("cachingConsumptionStatisticsService")
	private IConsumptionStatisticsService statisticsService;

	@Autowired
	private ITipRepository tipRepository;

	@Autowired
    private IAccountTipRepository accountTipRepository;

	@Autowired
	private IAccountAlertRepository accountAlertRepository;

	@Autowired
	private IAlertResolverExecutionRepository alertResolverExecutionRepository;

	@Autowired
	private IAccountRecommendationRepository accountRecommendationRepository;

	@Autowired
	private IRecommendationResolverExecutionRepository recommendationResolverExecutionRepository;

	/**
	 * Represent a specific target for message generation inside a utility.
	 */
	private class Target
	{
	    private class AccountEntityFinder
	        implements Transformer<UUID, AccountEntity>
	    {
            @Override
            public AccountEntity transform(UUID key)
            {
                return userRepository.getAccountByKey(key);
            }
	    }

	    private final UtilityInfo utility;

	    private final List<UUID> accountKeys;

	    private final boolean targetUtility;

	    /**
	     * Create a target for the entire utility
	     */
	    private Target(UtilityInfo utility)
	    {
	        this.utility = utility;
	        this.targetUtility = true;
	        this.accountKeys = utilityRepository.getMembers(utility.getId());
	    }

	    /**
         * Create a target for specific accounts into a given utility
         */
	    public Target(UtilityInfo utility, List<UUID> accountKeys)
	    {
	        this.utility = utility;
	        this.targetUtility = false; // targets a subset of this utility
	        this.accountKeys = ListUtils.intersection(
	            accountKeys,
	            utilityRepository.getMembers(utility.getId())
	        );
	    }

	    public List<UUID> getAccounts()
        {
            return accountKeys;
        }

	    public Iterable<AccountEntity> getAccountEntities()
	    {
	        return FluentIterable.of(accountKeys).transform(new AccountEntityFinder());
	    }

	    /**
	     * The number of accounts referenced by this target
	     */
	    public int size()
        {
            return accountKeys.size();
        }

	    /**
	     * Return a corresponding target entity, if it exists.
	     *
	     * In the case of a target to a custom set of accounts, {@code null} is returned.
	     */
	    public UtilityEntity asEntity()
	    {
	        return targetUtility? utilityRepository.findOne(utility.getId()) : null;
	    }
	}

	/**
	 * Provide an interface to utility-wide statistics.
	 *
	 * This is actually a decorator to the top-level statistics service.
	 */
	private class StatisticsService
	    implements IUtilityConsumptionStatisticsService
	{
	    private final UtilityInfo utility;

        public StatisticsService(UtilityInfo utility)
        {
            this.utility = utility;
        }

        @Override
        public ComputedNumber getNumber(
            LocalDateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
        {
            return statisticsService.getNumber(utility.getKey(), refDate, period, field, statistic);
        }

        @Override
        public ComputedNumber getNumber(
            DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
        {
            return statisticsService.getNumber(utility.getKey(), refDate, period, field, statistic);
        }
	}

	/**
	 * A utility-wide message generator
	 */
	private abstract class Generator
	    implements IGeneratorContext
	{
	    protected final UtilityInfo utility;

	    /** A reference date for resolvers */
	    protected final DateTime refDate;

	    /** The current day ending on refDate */
        protected final Interval refCurrentDay;

	    /** The current week ending on refDate */
	    protected final Interval refCurrentWeek;

	    /** The current month ending on refDate */
	    protected final Interval refCurrentMonth;

	    /** Provide statistics bound to this utility */
	    private final IUtilityConsumptionStatisticsService utilityStatisticsService;

	    protected Configuration config = new Configuration();

	    protected Generator(LocalDateTime refDate, UtilityInfo utility)
        {
	        this.utility = utility;

	        // Provide statistics bound to this utility

	        utilityStatisticsService =
	            DefaultMessageGeneratorService.this.new StatisticsService(utility);

	        // Determine reference date as a (zoned) DateTime

	        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());
	        DateTime end = this.refDate = refDate.toDateTime(tz);

	        // Compute sliding intervals ending on (not including) reference date

	        this.refCurrentDay = new Interval(
	            end.withTimeAtStartOfDay(), end);

	        this.refCurrentWeek = new Interval(
	            end.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay(), end);

	        this.refCurrentMonth = new Interval(
	            end.withDayOfMonth(1).withTimeAtStartOfDay(), end);
        }

	    public Generator configure(Configuration config)
	    {
	        this.config = config;
	        return this;
	    }

        @Override
        public DateTime getRefDate()
        {
            return refDate;
        }

        @Override
        public UtilityInfo getUtilityInfo()
        {
            return utility;
        }

        @Override
        public IUtilityConsumptionStatisticsService getStatsService()
        {
            return utilityStatisticsService;
        }

        protected DeviceExistsPredicate isDevicePresent(EnumDeviceType deviceType)
        {
            return new DeviceExistsPredicate(deviceType, deviceRepository);
        }

        protected <P extends Message.Parameters> boolean checkParameterizedTemplate(P p)
        {
            Set<ConstraintViolation<P>> constraintViolations = validator.validate(p);
            boolean valid = constraintViolations.isEmpty();
            if (!valid) {
                for (ConstraintViolation<P> c: constraintViolations) {
                    warn("Failed validation for parameterized template %s: at %s: %s",
                        p.getClass().getName(),
                        c.getPropertyPath(), c.getMessage());
                }
            }
            return valid;
        }

        public abstract void generate(Target target);
	}

	private class TipGenerator extends Generator
	{
        private static final int NUM_INITIAL_TIPS = 3;

	    public TipGenerator(UtilityInfo utility)
        {
            // Note: For tips, refDate is always now!
            super(LocalDateTime.now(), utility);
        }

        @Override
        public void generate(Target target)
        {
            info("About to generate tips for %d accounts in utility %s",
                target.size(), utility.getKey());

            DateTime t0 = refDate.minus(config.getTipPeriod());

            for (AccountEntity account: target.getAccountEntities()) {
                Locale locale = Locale.forLanguageTag(account.getLocale());
                // Select tips for this account
                List<TipEntity> tips = Collections.<TipEntity>emptyList();
                AccountTipEntity a = accountTipRepository.findLastForAccount(account.getKey());
                if (a == null) {
                    // No tips have ever been created for this account
                    tips = tipRepository.random(locale, NUM_INITIAL_TIPS);
                } else if (a.getCreatedOn().isBefore(t0)) {
                    // This account has received tips before a long time
                    TipEntity tip = tipRepository.randomOne(locale);
                    if (tip != null)
                        tips = Collections.singletonList(tip);
                }
                // Push to account-tip repository
                for (TipEntity tip: tips)
                    accountTipRepository.createWith(account, tip);
            }

            return;
        }
	}

	private class RecommendationGenerator extends Generator
	{
	    private IRecommendationResolverExecutionRepository resolverExecutionRepository;

        public RecommendationGenerator(LocalDateTime refDate, UtilityInfo utility)
        {
            super(refDate, utility);
            resolverExecutionRepository = recommendationResolverExecutionRepository;
        }

        @Override
        public void generate(Target target)
        {
            info("About to generate recommendations for %d accounts in utility %s",
                target.size(), utility.getKey());

            Map<String, IRecommendationResolver> resolvers = context.getBeansOfType(IRecommendationResolver.class);
            for (String resolverName: resolvers.keySet()) {
                if (recommendationsExcludedResolvers.contains(resolverName))
                    continue; // resolver is excluded by configuration
                IRecommendationResolver resolver = resolvers.get(resolverName);
                resolve(resolverName, resolver, target);
            }
        }

        private void resolve(String resolverName, IRecommendationResolver resolver, Target target)
        {
            MessageGenerator annotation = resolver.getClass().getAnnotation(MessageGenerator.class);
            if (annotation == null || !shouldExecute(resolverName, annotation))
                return; // not enabled as a resolver, or should not execute yet

            resolver.setup(config, this);
            info("About to resolve recommendations with %s (%s)",
                resolverName, resolver.getClass().getName());

            DateTime started = DateTime.now();
            RecommendationResolverExecutionEntity resolverExecutionEntity =
                resolverExecutionRepository.createWith(refDate, resolverName, target.asEntity(), started);

            FluentIterable<UUID> accountKeys = FluentIterable.of(target.getAccounts());

            Counters<EnumMessageLevel> counters = new Counters<>(EnumMessageLevel.class);

            for (EnumDeviceType deviceType: resolver.getSupportedDevices()) {
                for (UUID accountKey: accountKeys.filter(isDevicePresent(deviceType))) {
                    // Filter by checking per-account limits (throttle)
                    if (hasExceededPerAccountLimits(resolverName, annotation, accountKey, deviceType)) {
                        info("Skipping resolver %s for account %s: Too many messages",
                            resolverName, accountKey);
                        continue;
                    }

                    // Proceed and resolve alerts
                    List<MessageResolutionStatus<Recommendation.ParameterizedTemplate>> results = null;
                    try {
                        results = resolver.resolve(accountKey, deviceType);
                    } catch (RuntimeException x) {
                        error("Failed to resolve recommendations with %s for account %s: %s",
                            resolverName, accountKey, x);
                        x.printStackTrace();
                        results = null;
                    }
                    if (results == null || results.isEmpty())
                        continue;

                    // Validate and push resolved messages to repository
                    for (MessageResolutionStatus<Recommendation.ParameterizedTemplate> r: results) {
                        Recommendation.ParameterizedTemplate parameterizedTemplate = r.getMessage();
                        if (!checkParameterizedTemplate(parameterizedTemplate))
                            continue;
                        EnumMessageLevel level = r.isSignificant()?
                            EnumMessageLevel.NOTIFY : EnumMessageLevel.LOG;
                        counters.incr(level);
                        accountRecommendationRepository.createWith(
                            accountKey,
                            parameterizedTemplate,
                            resolverExecutionEntity,
                            deviceType,
                            level);
                    }
                }
            }

            resolver.teardown();
            info("Finished with recommendations examined by %s (%d new message(s))",
                resolverName, counters.get(EnumMessageLevel.NOTIFY));

            DateTime finished = DateTime.now();
            resolverExecutionRepository.updateFinished(resolverExecutionEntity, finished);

            return;
        }

        /**
         * Decide if a resolver should execute.
         *
         * @param resolverName
         * @param resolverAnnotation
         * @return
         */
        private boolean shouldExecute(String resolverName, MessageGenerator resolverAnnotation)
        {
            if (config.isOnDemandExecution())
                return true;

            // If a particular day-of-week is specified, check refDate is on that day.
            List<EnumDayOfWeek> daysOfWeek = Arrays.asList(resolverAnnotation.dayOfWeek());
            if (!daysOfWeek.isEmpty()) {
                EnumDayOfWeek day = EnumDayOfWeek.valueOf(refDate.getDayOfWeek());
                if (!daysOfWeek.contains(day))
                    return false;
            }

            // If a particular day-of-month is specified, check refDate is on that day.
            List<Integer> daysOfMonth =
                Arrays.asList(ArrayUtils.toObject(resolverAnnotation.dayOfMonth()));
            if (!daysOfMonth.isEmpty() && !daysOfMonth.contains(refDate.getDayOfMonth()))
                return false;

            // Otherwise (no day specified or refDate is on a trigger day), check if a successful
            // execution exists for the interval (refDate - P, refDate]

            Period period = Period.parse(resolverAnnotation.period());
            DateTime t0 = refDate.minus(period).plusMillis(1); // just after refDate - P

            List<Integer> xids =
                resolverExecutionRepository.findIdByName(resolverName, new Interval(t0, refDate));
            return xids.isEmpty();
        }

        /**
         * Decide if a resolver has exceeded per-account limits (maxPerDay, maxPerWeek, maxPerMonth).
         *
         * @param resolverName
         * @param resolverAnnotation
         * @param accountKey
         * @return
         */
        private boolean hasExceededPerAccountLimits(
            String resolverName, MessageGenerator resolverAnnotation, UUID accountKey, EnumDeviceType deviceType)
        {
            // Todo: Maybe should limit these counters to the specific deviceType

            List<Integer> xids;

            // Check for current day

            xids = resolverExecutionRepository.findIdByName(resolverName, refCurrentDay);
            if (!xids.isEmpty()) {
                int cnt = accountRecommendationRepository.countByAccountAndExecution(accountKey, xids);
                if (cnt >= resolverAnnotation.maxPerDay())
                    return true;
            }

            // Check for current week

            xids = resolverExecutionRepository.findIdByName(resolverName, refCurrentWeek);
            if (!xids.isEmpty()) {
                int cnt = accountRecommendationRepository.countByAccountAndExecution(accountKey, xids);
                if (cnt >= resolverAnnotation.maxPerWeek())
                    return true;
            }

            // Check for current month

            xids = resolverExecutionRepository.findIdByName(resolverName, refCurrentMonth);
            if (!xids.isEmpty()) {
                int cnt = accountRecommendationRepository.countByAccountAndExecution(accountKey, xids);
                if (cnt >= resolverAnnotation.maxPerMonth())
                    return true;
            }

            return false;
        }
	}

	private class AlertGenerator extends Generator
	{
        private IAlertResolverExecutionRepository resolverExecutionRepository;

	    public AlertGenerator(LocalDateTime refDate, UtilityInfo utility)
        {
            super(refDate, utility);
            resolverExecutionRepository = alertResolverExecutionRepository;
        }

        @Override
        public void generate(Target target)
        {
            info("About to generate alerts for %d accounts in utility %s",
                target.size(), utility.getKey());

            Map<String, IAlertResolver> resolvers = context.getBeansOfType(IAlertResolver.class);
            for (String resolverName: resolvers.keySet()) {
                if (alertsExcludedResolvers.contains(resolverName))
                    continue; // resolver is excluded by configuration
                IAlertResolver resolver = resolvers.get(resolverName);
                resolve(resolverName, resolver, target);
            }
        }

        private void resolve(String resolverName, IAlertResolver resolver, Target target)
        {
            MessageGenerator annotation = resolver.getClass().getAnnotation(MessageGenerator.class);
            if (annotation == null || !shouldExecute(resolverName, annotation))
                return; // not enabled as a resolver, or should not execute yet

            resolver.setup(config, this);
            info("About to resolve alerts with %s (%s)",
                resolverName, resolver.getClass().getName());

            DateTime started = DateTime.now();
            AlertResolverExecutionEntity resolverExecutionEntity =
                resolverExecutionRepository.createWith(refDate, resolverName, target.asEntity(), started);

            FluentIterable<UUID> accountKeys = FluentIterable.of(target.getAccounts());

            int cnt = 0;
            for (EnumDeviceType deviceType: resolver.getSupportedDevices()) {
                for (UUID accountKey: accountKeys.filter(isDevicePresent(deviceType))) {
                    // Filter by checking per-account limits (throttle)
                    if (hasExceededPerAccountLimits(resolverName, annotation, accountKey, deviceType)) {
                        info("Skipping resolver %s for account %s: Too many messages",
                            resolverName, accountKey);
                        continue;
                    }

                    // Proceed and resolve alerts
                    List<MessageResolutionStatus<Alert.ParameterizedTemplate>> results = null;
                    try {
                        results = resolver.resolve(accountKey, deviceType);
                    } catch (RuntimeException x) {
                        error("Failed to resolve alerts with %s for account %s: %s",
                            resolverName, accountKey, x);
                        x.printStackTrace();
                        results = null;
                    }
                    if (results == null || results.isEmpty())
                        continue;

                    // Validate and push resolved messages to repository
                    for (MessageResolutionStatus<Alert.ParameterizedTemplate> r: results) {
                        Alert.ParameterizedTemplate parameterizedTemplate = r.getMessage();
                        if (!checkParameterizedTemplate(parameterizedTemplate))
                            continue;
                        cnt++;
                        accountAlertRepository.createWith(
                            accountKey, parameterizedTemplate, resolverExecutionEntity, deviceType);
                    }
                }
            }

            resolver.teardown();
            info("Finished with alerts examined by %s (%d new message(s))",
                resolverName, cnt);

            DateTime finished = DateTime.now();
            resolverExecutionRepository.updateFinished(resolverExecutionEntity, finished);

            return;
        }

        /**
         * Decide if a resolver has exceeded per-account limits (maxPerDay, maxPerWeek, maxPerMonth).
         *
         * @param resolverName
         * @param resolverAnnotation
         * @param accountKey
         * @return
         */
        private boolean hasExceededPerAccountLimits(
            String resolverName, MessageGenerator resolverAnnotation, UUID accountKey, EnumDeviceType deviceType)
        {
            // Todo: Maybe should limit these counters to the specific deviceType

            List<Integer> xids;

            // Check for current day

            xids = resolverExecutionRepository.findIdByName(resolverName, refCurrentDay);
            if (!xids.isEmpty()) {
                int cnt = accountAlertRepository.countByAccountAndExecution(accountKey, xids);
                if (cnt >= resolverAnnotation.maxPerDay())
                    return true;
            }

            // Check for current week

            xids = resolverExecutionRepository.findIdByName(resolverName, refCurrentWeek);
            if (!xids.isEmpty()) {
                int cnt = accountAlertRepository.countByAccountAndExecution(accountKey, xids);
                if (cnt >= resolverAnnotation.maxPerWeek())
                    return true;
            }

            // Check for current month

            xids = resolverExecutionRepository.findIdByName(resolverName, refCurrentMonth);
            if (!xids.isEmpty()) {
                int cnt = accountAlertRepository.countByAccountAndExecution(accountKey, xids);
                if (cnt >= resolverAnnotation.maxPerMonth())
                    return true;
            }

            return false;
        }

        /**
         * Decide if a resolver should execute.
         *
         * @param resolverName
         * @param resolverAnnotation
         * @return
         */
        private boolean shouldExecute(String resolverName, MessageGenerator resolverAnnotation)
        {
            if (config.isOnDemandExecution())
                return true;

            // If a particular day-of-week is specified, check refDate is on that day.
            List<EnumDayOfWeek> daysOfWeek = Arrays.asList(resolverAnnotation.dayOfWeek());
            if (!daysOfWeek.isEmpty()) {
                EnumDayOfWeek day = EnumDayOfWeek.valueOf(refDate.getDayOfWeek());
                if (!daysOfWeek.contains(day))
                    return false;
            }

            // If a particular day-of-month is specified, check refDate is on that day.
            List<Integer> daysOfMonth =
                Arrays.asList(ArrayUtils.toObject(resolverAnnotation.dayOfMonth()));
            if (!daysOfMonth.isEmpty() && !daysOfMonth.contains(refDate.getDayOfMonth()))
                return false;

            // Otherwise (no day-of-X specified or refDate is on a trigger day), check
            // if a successful execution exists for the interval (refDate - P, refDate]

            Period period = Period.parse(resolverAnnotation.period());
            DateTime t0 = refDate.minus(period).plusMillis(1); // just after refDate - P

            List<Integer> xids =
                resolverExecutionRepository.findIdByName(resolverName, new Interval(t0, refDate));
            return xids.isEmpty();
        }

	}

	private static class DeviceExistsPredicate implements Predicate<UUID>
	{
	    private final EnumDeviceType deviceType;

	    private final IDeviceRepository deviceRepository;

	    public DeviceExistsPredicate(EnumDeviceType deviceType, IDeviceRepository deviceRepository)
        {
            this.deviceType = deviceType;
            this.deviceRepository = deviceRepository;
        }

        @Override
	    public boolean evaluate(UUID accountKey)
	    {
            List<Device> devices = deviceRepository.getUserDevices(accountKey, deviceType);
	        return !devices.isEmpty();
	    }
	}

	@Override
	public void executeUtility(LocalDateTime refDate, Configuration config, UUID utilityKey)
	{
	    UtilityInfo utility = utilityRepository.getUtilityByKey(utilityKey);
	    generate(refDate, config, utility, new Target(utility));
	}

	@Override
	public void executeAccounts(LocalDateTime refDate, Configuration config, List<UUID> accountKeys)
	{
	    for (UtilityInfo utility: utilityRepository.getUtilities()) {
	        generate(refDate, config, utility, new Target(utility, accountKeys));
	    }
	}

	private void generate(LocalDateTime refDate, Configuration config, UtilityInfo utility, Target target)
	{
	    Generator generator = null;

	    // Generate tips

	    if (tipsEnabled) {
	        generator = new TipGenerator(utility)
	            .configure(config);
	        generator.generate(target);
	    }

	    // Generate alerts

	    if (alertsEnabled) {
	        generator = new AlertGenerator(refDate, utility)
	            .configure(config);
	        generator.generate(target);
	    }

	    // Generate recommendations

	    if (recommendationsEnabled) {
	        generator = new RecommendationGenerator(refDate, utility)
	            .configure(config);
	        generator.generate(target);
	    }

        return;
	}

	private static void error(String f, Object ...args)
	{
	    logger.error(String.format(f, args));
	}

	private static void warn(String f, Object ...args)
    {
        logger.warn(String.format(f, args));
    }

	private static void info(String f, Object ...args)
    {
        if (logger.isInfoEnabled())
            logger.info(String.format(f, args));
    }
}
