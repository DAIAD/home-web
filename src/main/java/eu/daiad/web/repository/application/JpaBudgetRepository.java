package eu.daiad.web.repository.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.BudgetAccountEntity;
import eu.daiad.web.domain.application.BudgetEntity;
import eu.daiad.web.domain.application.BudgetSnapshotAccountEntity;
import eu.daiad.web.domain.application.BudgetSnapshotEntity;
import eu.daiad.web.domain.application.SavingsPotentialAccountEntity;
import eu.daiad.web.domain.application.SavingsPotentialScenarioEntity;
import eu.daiad.web.domain.application.mappings.BudgetSegmentEntity;
import eu.daiad.web.job.builder.BudgetProcessingJobBuilder;
import eu.daiad.web.model.error.BudgetErrorCode;
import eu.daiad.web.model.error.SavingsPotentialErrorCode;
import eu.daiad.web.model.query.savings.Budget;
import eu.daiad.web.model.query.savings.BudgetExploreConsumerResult;
import eu.daiad.web.model.query.savings.BudgetParameters;
import eu.daiad.web.model.query.savings.BudgetQuery;
import eu.daiad.web.model.query.savings.BudgetQueryResult;
import eu.daiad.web.model.query.savings.EnumBudgetStatus;
import eu.daiad.web.model.query.savings.SavingsConsumerSelectionFilter;
import eu.daiad.web.repository.BaseRepository;
import eu.daiad.web.service.savings.ConsumerSelectionUtils;
import eu.daiad.web.service.scheduling.ISchedulerService;

@Repository
@Transactional("applicationTransactionManager")
public class JpaBudgetRepository extends BaseRepository implements IBudgetRepository, InitializingBean {

    /**
     * Java Persistence entity manager.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    /**
     * Service for querying, scheduling and launching jobs.
     */
    @Autowired
    private ISchedulerService schedulerService;

    /**
     * A builder used to create {@link ObjectMapper} instances for serializing budget parameters.
     */
    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    /**
     * Object mapper for serializing budget parameters.
     */
    private ObjectMapper objectMapper;

    /**
     * Helper service for filtering users based on an instance of {@link SavingsConsumerSelectionFilter}.
     */
    @Autowired
    private ConsumerSelectionUtils consumerSelectionUtils;

    @Override
    public void afterPropertiesSet() throws Exception {
        objectMapper = jackson2ObjectMapperBuilder.build();
    }

    /**
     * Creates a new budget.
     *
     * @param ownerId the owner account id.
     * @param name a user-friendly name for the budget.
     * @param parameters parameters for selecting user data.
     * @return the new budget key.
     * @throws JsonProcessingException if parameters serialization fails
     */
    @Override
    public UUID createBudget(int ownerId, String name, BudgetParameters parameters) throws JsonProcessingException {
        AccountEntity owner = getAccountById(ownerId);

        // Create budget
        BudgetEntity budget = new BudgetEntity();

        budget.setOwner(owner);
        budget.setUtility(owner.getUtility());
        budget.setName(name);
        budget.setActive(false);
        budget.setInitialized(false);

        // Always a scenario is selected over a goal
        if ((parameters.getScenario() != null) && (parameters.getScenario().getKey() != null)) {
            SavingsPotentialScenarioEntity scenario = getScenarioByKey(parameters.getScenario().getKey());

            budget.setScenario(scenario);
            budget.setScenarioPercent(parameters.getScenario().getPercent());

        } else {
            budget.setGoal(parameters.getGoal());
        }
        budget.setDistribution(parameters.getDistribution());
        budget.setParameters(objectMapper.writeValueAsString(parameters));

        entityManager.persist(budget);

        return budget.getKey();
    }

    /**
     * Initializes the budget consumers.
     *
     * @param budgetKey the budget key.
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Override
    public void initializeBudgetConsumers(UUID budgetKey) throws JsonParseException, JsonMappingException, IOException {
        BudgetEntity budget = getBudgetByKey(budgetKey);
        BudgetParameters parameters = objectMapper.readValue(budget.getParameters(), new TypeReference<BudgetParameters>() { });

        double consumption = 0;
        double savings = 0;

        List<UUID> userKeys = selectConsumers(parameters);
        for (UUID userKey : userKeys) {
            AccountEntity account = getAccountByKey(userKey);

            BudgetAccountEntity budgetAccount = new BudgetAccountEntity();
            budgetAccount.setAccount(account);
            budgetAccount.setBudget(budget);
            entityManager.persist(budgetAccount);

            if(budget.getScenario() != null) {
                String queryString = "SELECT a FROM savings_potential_account a where a.scenario.key = :scenarioKey and a.account.key = :userKey";
                SavingsPotentialAccountEntity scenarioAccount = entityManager.createQuery(queryString, SavingsPotentialAccountEntity.class)
                                                                             .setParameter("scenarioKey", budget.getScenario().getKey())
                                                                             .setParameter("userKey", userKey)
                                                                             .getSingleResult();

                consumption += scenarioAccount.getConsumption();
                savings += scenarioAccount.getSavingsVolume();
            }
        }

        if (budget.getScenario() == null) {
            budget.setExpectedPercent((double) budget.getGoal() / 100);
        } else {
            if ((consumption > 0) && (savings < consumption)) {
                double expectedPercent = (savings / consumption) * ((double) budget.getScenarioPercent() / 100D);
                budget.setExpectedPercent(expectedPercent);
            } else {
                budget.setExpectedPercent(0D);
            }
        }
        budget.setNumberOfConsumers(userKeys.size());

        budget.setInitialized(true);
    }

    /**
     * Resets a snapshot for an active budget.
     *
     * @param key the unique budget key.
     * @param year reference date year.
     * @param month reference date month.
     */
    @Override
    public void deleteSnapshot(UUID key, int year, int month) {
        BudgetEntity budget = getBudgetByKey(key);

        entityManager.createQuery("delete from budget_snapshot where budget_id = :budget_id and year = :year and month = :month")
                     .setParameter("budget_id", budget.getId())
                     .setParameter("year", year)
                     .setParameter("month", month)
                     .executeUpdate();
    }

    /**
     * Loads a budget given its key.
     *
     * @param key the unique budget key.
     * @return the budget.
     */
    @Override
    public BudgetEntity getBudgetByKey(UUID key) {
        String queryString = "select b from budget b where b.key = :key";

        TypedQuery<BudgetEntity> query = entityManager.createQuery(queryString, BudgetEntity.class)
                                                      .setParameter("key", key);

        List<BudgetEntity> result = query.getResultList();
        if (result.isEmpty()) {
            throw createApplicationException(BudgetErrorCode.BUDGET_NOT_FOUND);
        }
        return result.get(0);
    }

    /**
     * Gets all active budgets.
     *
     * @return a list of {@link BudgetEntity} objects.
     */
    @Override
    public List<BudgetEntity> getActiveBudgets() {
        String queryString = "select b from budget b where b.active = true";

        return entityManager.createQuery(queryString, BudgetEntity.class).getResultList();
    }

    /**
     * Gets all pending budget snapshots.
     *
     * @return a list of {@link BudgetSnapshotEntity}.
     */
    @Override
    public List<BudgetSnapshotEntity> findPendingSnapshots() {
        String queryString = "select s from budget_snapshot s where s.status = :status";

        return entityManager.createQuery(queryString, BudgetSnapshotEntity.class)
                            .setParameter("status",  EnumBudgetStatus.PENDING)
                            .getResultList();
    }

    /**
     * Loads a budget given its id.
     *
     * @param id the budget id.
     * @return the budget.
     */
    @Override
    public BudgetEntity getBudgetById(long id) {
        String queryString = "select b from budget b where b.id = :id";

        TypedQuery<BudgetEntity> query = entityManager.createQuery(queryString, BudgetEntity.class)
                                                      .setParameter("id", id);

        List<BudgetEntity> result = query.getResultList();
        if (result.isEmpty()) {
            throw createApplicationException(BudgetErrorCode.BUDGET_NOT_FOUND);
        }
        return result.get(0);
    }

    /**
     * Deletes an existing budget given its key.
     *
     * @param key the unique budget key.
     */
    @Override
    public void deleteBudgetByKey(UUID key) {
        BudgetEntity budget = getBudgetByKey(key);

        entityManager.createQuery("delete from budget where id = :budget_id")
                     .setParameter("budget_id", budget.getId())
                     .executeUpdate();
    }

    /**
     * Sets active state for a budget.
     *
     * @param key the budget key.
     * @param active the new active state value.
     */
    @Override
    public void setActive(UUID key, boolean active) {
        BudgetEntity budget = getBudgetByKey(key);

        if (active) {
            if(!budget.isActive()) {
                budget.setActivatedOn(DateTime.now());
            }
        } else {
            budget.setActivatedOn(null);
        }
        budget.setActive(active);

        entityManager.flush();
    }

    /**
     * Filter budget using a query.
     *
     * @param query the query.
     * @return a collection of {@link Budget}.
     */
    @Override
    public BudgetQueryResult query(BudgetQuery query) {
        if (query == null) {
            return new BudgetQueryResult(0, 10);
        }

        if ((query.getPageIndex() == null) || (query.getPageIndex() < 0)) {
            query.setPageIndex(0);
        }
        if ((query.getPageSize() == null) || (query.getPageSize() < 1)) {
            query.setPageSize(10);
        }

        String command = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();

        filters.add("(b.owner.id = :account_id)");

        if (!StringUtils.isBlank(query.getName())) {
            filters.add("(b.name like :name)");
        }
        if (query.getActive() != null) {
            filters.add("b.active = :active");
        }

        // Count total number of records
        Integer count;
        command = "select count(b.id) from budget b ";

        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);

        countQuery.setParameter("account_id", query.getOwnerId());
        if (!StringUtils.isBlank(query.getName())) {
            countQuery.setParameter("name", query.getName() + "%");
        }
        if (query.getActive() != null) {
            countQuery.setParameter("active", query.getActive());
        }

        count = countQuery.getSingleResult().intValue();

        // Load data
        command = "select b from budget b ";

        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        switch (query.getSortBy()) {
            case NAME:
                command += " order by b.name ";
                break;
            case CREATED_ON:
                command += " order by b.createdOn ";
                break;
            case ACTIVE:
                command += " order by b.active ";
                break;
        }
        if (query.isSortAscending()) {
            command += " asc";
        } else {
            command += " desc";
        }

        TypedQuery<BudgetEntity> selectQuery = entityManager.createQuery(command, BudgetEntity.class);

        selectQuery.setParameter("account_id", query.getOwnerId());
        if (!StringUtils.isBlank(query.getName())) {
            selectQuery.setParameter("name", query.getName() + "%");
        }
        if (query.getActive() != null) {
            countQuery.setParameter("active", query.getActive());
        }

        selectQuery.setFirstResult(query.getPageIndex() * query.getPageSize());
        selectQuery.setMaxResults(query.getPageSize());

        List<BudgetEntity> entities = selectQuery.getResultList();
        List<Budget> budgets = new ArrayList<Budget>();
        for (BudgetEntity entity : entities) {
            try {
                budgets.add(new Budget(entity, objectMapper));
            } catch (IOException e) {
                throw createApplicationException(SavingsPotentialErrorCode.PARSE_ERROR);
            }
        }

        return new BudgetQueryResult(query.getPageIndex(), query.getPageSize(), count, budgets);

    }

    /**
     * Updates budget snapshot consumption values.
     *
     * @param key the budget key.
     * @param jobId the id of the job that created the snapshot.
     * @param year reference date year.
     * @param month reference date month.
     * @return the new snapshot id.
     */
    @Override
    public long createSnapshot(UUID key, long jobId, int year, int month) {
        BudgetEntity budget = getBudgetByKey(key);

        BudgetSnapshotEntity snapshot = new BudgetSnapshotEntity();
        snapshot.setBudget(budget);
        snapshot.setJobId(jobId);
        snapshot.setYear(year);
        snapshot.setMonth(month);
        snapshot.setProcessingDateBegin(DateTime.now());
        snapshot.setStatus(EnumBudgetStatus.RUNNING);

        entityManager.persist(snapshot);

        return snapshot.getId();
    }

    /**
     * Updates budget snapshot consumption values and sets its state to COMPLETED.
     *
     * @param key the budget key.
     * @param snapshotId the snapshot id.
     * @param consumptionBefore previous consumption volume.
     * @param consumptionAfter current consumption volume.
     * @param updatedOn update timestamp.
     */
    @Override
    public void updateSnapshot(UUID key, long snapshotId, double consumptionBefore, double consumptionAfter, DateTime updatedOn)  {
        BudgetEntity budget = getBudgetByKey(key);

        BudgetSnapshotEntity snapshot = null;
        for (BudgetSnapshotEntity s : budget.getSnapshots()) {
            if (s.getId() == snapshotId) {
                snapshot = s;
                break;
            }
        }
        if(snapshot == null ) {
            throw createApplicationException(BudgetErrorCode.BUDGET_SNAPSHOT_NOT_FOUND);
        }

        snapshot.setConsumptionBefore(consumptionBefore);
        snapshot.setConsumptionAfter(consumptionAfter);
        snapshot.setPercent(consumptionBefore > 0 ? (consumptionBefore - consumptionAfter) / consumptionBefore : 0);
        snapshot.setStatus(EnumBudgetStatus.COMPLETED);
        snapshot.setProcessingDateEnd(updatedOn);
        snapshot.setExpectedPercent(computeSnapshotExpectedPercent(key));
        budget.setUpdatedOn(updatedOn);
        budget.setNextUpdateOn(schedulerService.getJobNextExecutionDateTime(BudgetProcessingJobBuilder.JOB_NAME));

        entityManager.flush();
    }

    /**
     * Updates budget snapshot and sets its state to FAILED.
     *
     * @param key the budget key.
     * @param snapshotId the snapshot id.
     * @param updatedOn update timestamp.
     */
    @Override
    public void updateSnapshot(UUID key, long snapshotId, DateTime updatedOn) {
        BudgetEntity budget = getBudgetByKey(key);

        BudgetSnapshotEntity snapshot = null;
        for (BudgetSnapshotEntity s : budget.getSnapshots()) {
            if (s.getId() == snapshotId) {
                snapshot = s;
                break;
            }
        }
        if(snapshot == null ) {
            throw createApplicationException(BudgetErrorCode.BUDGET_SNAPSHOT_NOT_FOUND);
        }

        snapshot.setStatus(EnumBudgetStatus.FAILED);
        snapshot.setProcessingDateEnd(updatedOn);

        budget.setUpdatedOn(updatedOn);
        budget.setNextUpdateOn(schedulerService.getJobNextExecutionDateTime(BudgetProcessingJobBuilder.JOB_NAME));

        entityManager.flush();
    }

    /**
     * Updates budget user consumption values.
     *
     * @param budgetKey the budget key.
     * @param snapshotId the snapshot id.
     * @param userKey the user key.
     * @param consumptionBefore previous consumption volume.
     * @param consumptionAfter current consumption volume.
     * @param updatedOn update timestamp.
     */
    @Override
    public void updateConsumer(UUID budgetKey, long snapshotId, UUID userKey, double consumptionBefore, double consumptionAfter, DateTime updatedOn) {
        BudgetEntity budget = getBudgetByKey(budgetKey);

        BudgetSnapshotEntity snapshot = null;
        for (BudgetSnapshotEntity s : budget.getSnapshots()) {
            if (s.getId() == snapshotId) {
                snapshot = s;
                break;
            }
        }
        if(snapshot == null ) {
            throw createApplicationException(BudgetErrorCode.BUDGET_SNAPSHOT_NOT_FOUND);
        }

        AccountEntity account = getAccountByKey(userKey);

        BudgetSnapshotAccountEntity entity = new BudgetSnapshotAccountEntity();
        entity.setAccount(account);
        entity.setSnapshot(snapshot);
        entity.setConsumptionBefore(consumptionBefore);
        entity.setConsumptionAfter(consumptionAfter);
        entity.setPercent(consumptionBefore > 0 ? (consumptionBefore - consumptionAfter) / consumptionBefore : 0);
        entity.setExpectedPercent(computeConsumerExpectedPercent(budgetKey, userKey));

        entityManager.persist(entity);
    }

    /**
     * Compute consumption per cluster segment for a given budget and cluster.
     *
     * @param budgetKey the budget key.
     * @param clusterKey the cluster key.
     * @return a list of {@link BudgetSegmentEntity}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<BudgetSegmentEntity> exploreCluster(UUID budgetKey, UUID clusterKey) {
        String queryString = "select   g.id as id, " +
                        "              c.name as cluster_name, c.key as cluster_key, " +
                        "              g.name as segment_name, g.key as segment_key, " +
                        "              sum(a.consumption_volume_before) as consumption_before, " +
                        "              sum(a.consumption_volume_after) as consumption_after " +
                        "from          budget b " +
                        "                  inner join budget_snapshot s on b.id = s.budget_id " +
                        "                  inner join budget_account_snapshot a on a.budget_snapshot_id = s.id " +
                        "                  inner join group_member m on a.account_id = m.account_id " +
                        "                  inner join group_segment gs on m.group_id = gs.id " +
                        "                  inner join \"cluster\" c on gs.cluster_id = c.id " +
                        "                  inner join \"group\" g on gs.id = g.id         " +
                        "where         b.key = cast(?1 as uuid) and " +
                        "              c.key = cast(?2 as uuid) " +
                        "group by      g.id, c.name, c.key, g.name, g.key " +
                        "order by      g.name";


        Query query = entityManager.createNativeQuery(queryString, "BudgetClusterSegmentResult")
                                   .setParameter(1, budgetKey)
                                   .setParameter(2, clusterKey);

        return (List<BudgetSegmentEntity>) query.getResultList();
    }


    /**
     * Query consumer consumption data for a given budget and user.
     *
     * @param budgetKey the budget key.
     * @param consumerKey the consumer key.
     * @return a list of {@link BudgetSegmentEntity}.
     */
    @Override
    public BudgetExploreConsumerResult exploreConsumer(UUID budgetKey, UUID consumerKey) {
        AccountEntity account = getAccountByKey(consumerKey);

        String queryString = "select a from budget_account_snapshot a " +
                             "where  a.account.key = :consumerKey and a.snapshot.budget.key = :budgetKey";

        TypedQuery<BudgetSnapshotAccountEntity> query = entityManager.createQuery(queryString, BudgetSnapshotAccountEntity.class)
                                                                     .setParameter("consumerKey", consumerKey)
                                                                     .setParameter("budgetKey", budgetKey);

        return new BudgetExploreConsumerResult(account, query.getResultList());
    }

    /**
     * Gets all budget consumers user keys.
     *
     * @param budgetKey the budget key.
     * @return a list of {@link UUID}.
     */
    @Override
    public List<UUID> getBudgetMembers(UUID key) {
        String queryString = "SELECT a.account.key FROM budget_account a where a.budget.key = :key";

        return entityManager.createQuery(queryString, UUID.class)
                            .setParameter("key", key)
                            .getResultList();
    }

    /**
     * Resets status for all savings scenarios whose processing has been
     * interrupted.
     */
    @Override
    public void cleanStatus() {
        List<EnumBudgetStatus> status = new ArrayList<EnumBudgetStatus>();
        for (EnumBudgetStatus value : EnumBudgetStatus.values()) {
            if ((value != EnumBudgetStatus.COMPLETED) && (value != EnumBudgetStatus.FAILED)) {
                status.add(value);
            }
        }

        String queryString = "select s from budget_snapshot s where s.status in :status";

        TypedQuery<BudgetSnapshotEntity> query = entityManager.createQuery(queryString, BudgetSnapshotEntity.class)
                                                              .setParameter("status", status);

        for(BudgetSnapshotEntity snapshot : query.getResultList()) {
            // Schedule again
            snapshot.setStatus(EnumBudgetStatus.PENDING);
        }
    }

    private AccountEntity getAccountById(int id) {
        String queryString = "SELECT a FROM account a where a.id = :id";

        return entityManager.createQuery(queryString, AccountEntity.class)
                            .setParameter("id", id)
                            .getSingleResult();
    }

    private AccountEntity getAccountByKey(UUID key) {
        String queryString = "SELECT a FROM account a where a.key = :key";

        return entityManager.createQuery(queryString, AccountEntity.class)
                            .setParameter("key", key)
                            .getSingleResult();
    }

    private SavingsPotentialScenarioEntity getScenarioByKey(UUID key) {
        String queryString = "select s from savings_potential_scenario s where s.key = :key";

        TypedQuery<SavingsPotentialScenarioEntity> query = entityManager.createQuery(queryString, SavingsPotentialScenarioEntity.class)
                                                                        .setParameter("key", key);

        List<SavingsPotentialScenarioEntity> result = query.getResultList();
        if (result.isEmpty()) {
            throw createApplicationException(SavingsPotentialErrorCode.SCENARIO_NOT_FOUND);
        }
        return result.get(0);
    }

    /**
     * Given a budget parameters {@link BudgetParameters}, return all selected
     * consumer account keys.
     *
     * @param parameters budget parameters.
     * @return a list of user keys.
     */
    private List<UUID> selectConsumers(BudgetParameters parameters) {
        List<UUID> included = new ArrayList<UUID>();
        List<UUID> excluded = new ArrayList<UUID>();


        if(parameters == null) {
            return included;
        }

        if(parameters.getScenario() == null) {
            included = consumerSelectionUtils.expandUsers(parameters.getInclude());
        } else {
            included = selectConsumersFromScenario(parameters.getScenario().getKey());
        }

        if (parameters.getExclude() != null) {
            excluded = consumerSelectionUtils.expandUsers(parameters.getExclude());
        }

        included.removeAll(excluded);
        return included;
    }

    /**
     * Selects all consumers from an existing scenario.
     *
     * @param key the scenario key.
     * @return a list of user keys.
     */
    private List<UUID> selectConsumersFromScenario(UUID key) {
        if(key== null) {
            return new ArrayList<UUID>();
        }

        String queryString = "SELECT a.account.key FROM savings_potential_account a where a.scenario.key = :key";

        return entityManager.createQuery(queryString, UUID.class)
                            .setParameter("key", key)
                            .getResultList();
    }

    /**
     * Computes the expected savings percent for a budget.
     *
     * @param budgetKey the budget key.
     * @return the percent value.
     */
    private double computeSnapshotExpectedPercent(UUID budgetKey)  {
        BudgetEntity budget = getBudgetByKey(budgetKey);

        if(budget.getScenario() == null) {
            return (double) budget.getGoal() / 100D;
        }

        List<UUID> userKeys = getBudgetMembers(budgetKey);

        double consumption = 0;
        double savings = 0;

        for (UUID userKey : userKeys) {
            String queryString = "SELECT a FROM savings_potential_account a where a.scenario.key = :scenarioKey and a.account.key = :userKey";
            SavingsPotentialAccountEntity scenarioAccount = entityManager.createQuery(queryString, SavingsPotentialAccountEntity.class)
                                                                         .setParameter("scenarioKey", budget.getScenario().getKey())
                                                                         .setParameter("userKey", userKey)
                                                                         .getSingleResult();

            consumption += scenarioAccount.getConsumption();
            savings += scenarioAccount.getSavingsVolume();
        }

        if ((consumption > 0) && (savings < consumption)) {
            return (savings / consumption) * ((double) budget.getScenarioPercent() / 100D);
        } else {
            return 0D;
        }
    }

    /**
     * Computes the expected savings percent for a budget.
     *
     * @param budgetKey the budget key.
     * @param userKey the consumer user key.
     * @return the percent value.
     */
    private double computeConsumerExpectedPercent(UUID budgetKey, UUID userKey)  {
        BudgetEntity budget = getBudgetByKey(budgetKey);

        if(budget.getScenario() == null) {
            return (double) budget.getGoal() / 100D;
        }

        String queryString = "SELECT a FROM savings_potential_account a where a.scenario.key = :scenarioKey and a.account.key = :userKey";
        SavingsPotentialAccountEntity scenarioAccount = entityManager.createQuery(queryString, SavingsPotentialAccountEntity.class)
                                                                     .setParameter("scenarioKey", budget.getScenario().getKey())
                                                                     .setParameter("userKey", userKey)
                                                                     .getSingleResult();

        if ((scenarioAccount.getConsumption() > 0) && (scenarioAccount.getSavingsVolume() < scenarioAccount.getConsumption())) {
            return (scenarioAccount.getSavingsVolume() / scenarioAccount.getConsumption()) * ((double) budget.getScenarioPercent() / 100D);
        } else {
            return 0D;
        }
    }
}
