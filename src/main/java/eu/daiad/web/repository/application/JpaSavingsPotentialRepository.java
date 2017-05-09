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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.SavingsPotentialAccountEntity;
import eu.daiad.web.domain.application.SavingsPotentialResultEntity;
import eu.daiad.web.domain.application.SavingsPotentialScenarioEntity;
import eu.daiad.web.domain.application.SavingsPotentialWaterIqEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.domain.application.mappings.SavingScenarioSegmentEntity;
import eu.daiad.web.model.error.SavingsPotentialErrorCode;
import eu.daiad.web.model.query.savings.EnumSavingScenarioStatus;
import eu.daiad.web.model.query.savings.SavingScenario;
import eu.daiad.web.model.query.savings.SavingScenarioQuery;
import eu.daiad.web.model.query.savings.SavingScenarioQueryResult;
import eu.daiad.web.model.query.savings.SavingsCluster;
import eu.daiad.web.model.query.savings.SavingsClusterCollection;
import eu.daiad.web.model.query.savings.SavingsClusterMonth;
import eu.daiad.web.model.query.savings.TemporalSavingsConsumerSelectionFilter;
import eu.daiad.web.repository.BaseRepository;

/**
 * Provides methods for accessing savings potential and water IQ data.
 */
@Repository
@Transactional("applicationTransactionManager")
public class JpaSavingsPotentialRepository extends BaseRepository implements ISavingsPotentialRepository, InitializingBean {

    /**
     * Java Persistence entity manager.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    /**
     * A builder used to create {@link ObjectMapper} instances for serializing scenario parameters.
     */
    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    /**
     * Object mapper for serializing scenario parameters.
     */
    private ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        objectMapper = jackson2ObjectMapperBuilder.build();
    }

    /**
     * Creates a new savings potential scenario
     *
     * @param ownerId the owner account id.
     * @param name a user-friendly name for the scenario.
     * @param parameters parameters for selecting user data.
     * @return the new scenario key.
     * @throws JsonProcessingException if parameters serialization fails
     */
    @Override
    public UUID create(int ownerId, String name, TemporalSavingsConsumerSelectionFilter parameters) throws JsonProcessingException {
        AccountEntity owner = getAccountById(ownerId);

        SavingsPotentialScenarioEntity scenario = new SavingsPotentialScenarioEntity();
        scenario.setOwner(owner);
        scenario.setUtility(owner.getUtility());
        scenario.setName(name);
        scenario.setParameters(objectMapper.writeValueAsString(parameters));
        scenario.setStatus(EnumSavingScenarioStatus.PENDING);

        entityManager.persist(scenario);

        return scenario.getKey();
    }


    /**
     * Resets the results of a savings potential scenario.
     *
     * @param key the unique scenario key.
     */
    @Override
    public void reset(UUID key) {
        SavingsPotentialScenarioEntity scenario = getScenarioByKey(key);

        entityManager.createQuery("delete from savings_potential_result where scenario_id = :scenario_id")
                     .setParameter("scenario_id", scenario.getId())
                     .executeUpdate();

        entityManager.createQuery("delete from savings_potential_account where scenario_id = :scenario_id")
                     .setParameter("scenario_id", scenario.getId())
                     .executeUpdate();

        scenario.setStatus(EnumSavingScenarioStatus.PENDING);
        scenario.setProcessingDateBegin(null);
        scenario.setProcessingDateEnd(null);
        scenario.setJobId(null);
    }

    /**
     * Loads a savings potential scenario given its key.
     *
     * @param key the unique scenario key.
     * @return the scenario.
     */
    @Override
    public SavingsPotentialScenarioEntity getScenarioByKey(UUID key) {
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
     * Loads a savings potential scenario given its id.
     *
     * @param id the scenario id.
     * @return the scenario.
     */
    @Override
    public SavingsPotentialScenarioEntity getScenarioById(long id) {
        String queryString = "select s from savings_potential_scenario s where s.id = :id";

        TypedQuery<SavingsPotentialScenarioEntity> query = entityManager.createQuery(queryString, SavingsPotentialScenarioEntity.class)
                                                                        .setParameter("id", id);

        List<SavingsPotentialScenarioEntity> result = query.getResultList();
        if (result.isEmpty()) {
            throw createApplicationException(SavingsPotentialErrorCode.SCENARIO_NOT_FOUND);
        }
        return result.get(0);
    }

    /**
     * Updates scenario job execution.
     *
     * @param key the scenario key.
     * @param status new status value.
     * @param updatedOn update timestamp.
     */
    @Override
    public void updateJobExecution(UUID key, EnumSavingScenarioStatus status, DateTime updatedOn) {
        SavingsPotentialScenarioEntity scenario = getScenarioByKey(key);

        switch(status) {
            case RUNNING:
                if(scenario.getStatus() == EnumSavingScenarioStatus.PENDING) {
                    scenario.setProcessingDateBegin(updatedOn);
                }
                break;
            case COMPLETED: case FAILED:
                scenario.setProcessingDateEnd(updatedOn);
                break;
            default:
                // Ignore
        }
        scenario.setStatus(status);

        entityManager.flush();
    }

    /**
     * Updates scenario savings values.
     *
     * @param key the scenario key.
     * @param consumption total consumption for all consumers in the scenario time interval.
     * @param saved savings potential volume.
     * @param updatedOn update timestamp.
     * @param numberOfCosnumers number of consumers.
     */
    @Override
    public void updateSavingScenario(UUID key, double consumption, double saved, DateTime updatedOn, int numberOfCosnumers) {
        SavingsPotentialScenarioEntity scenario = getScenarioByKey(key);

        scenario.setConsumption(consumption);
        scenario.setSavingsVolume(saved);
        scenario.setSavingsPercent(consumption > 0 && saved < consumption ? saved / consumption : 0);
        scenario.setProcessingDateBegin(updatedOn);
        scenario.setNumberOfConsumers(numberOfCosnumers);

        entityManager.flush();
    }

    /**
     * Updates scenario user savings values.
     *
     * @param scenarioKey the scenario key.
     * @param userKey the user key.
     * @param consumption consumption volume.
     * @param saved savings volume.
     * @param updatedOn update timestamp.
     */
    @Override
    public void updateSavingConsumer(UUID scenarioKey, UUID userKey, double consumption, double saved, DateTime updatedOn) {
        SavingsPotentialScenarioEntity scenario = getScenarioByKey(scenarioKey);

        String queryString = "select a from account a where a.key = :key";

        AccountEntity account = entityManager.createQuery(queryString, AccountEntity.class)
                                             .setParameter("key", userKey)
                                             .getSingleResult();

        SavingsPotentialAccountEntity entity = new SavingsPotentialAccountEntity();
        entity.setScenario(scenario);
        entity.setAccount(account);
        entity.setSavingsPercent(consumption > 0 && saved < consumption ? saved / consumption : 0);
        entity.setSavingsVolume(saved);
        entity.setConsumption(consumption);

        entityManager.persist(entity);
    }

    /**
     * Deletes an existing savings potential scenario given its key.
     *
     * @param key the unique scenario key.
     */
    @Override
    public void deleteScenarioByKey(UUID key) {
        SavingsPotentialScenarioEntity scenario = getScenarioByKey(key);

        entityManager.createQuery("delete from savings_potential_result where scenario_id = :scenario_id")
                     .setParameter("scenario_id", scenario.getId())
                     .executeUpdate();

        entityManager.createQuery("delete from savings_potential_account where scenario_id = :scenario_id")
                     .setParameter("scenario_id", scenario.getId())
                     .executeUpdate();

        entityManager.remove(scenario);
        entityManager.flush();
    }

    /**
     * Filter savings potential scenario using a query.
     *
     * @param query the query.
     * @return a collection of {@link SavingScenario}.
     */
    @Override
    public SavingScenarioQueryResult query(SavingScenarioQuery query) {
        if (query == null) {
            return new SavingScenarioQueryResult(0, 10);
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

        filters.add("(s.owner.id = :account_id)");

        if (!StringUtils.isBlank(query.getName())) {
            filters.add("(s.name like :name)");
        }
        if (!query.getStatus().equals(EnumSavingScenarioStatus.UNDEFINED)) {
            filters.add("s.status = :status");
        }

        // Count total number of records
        Integer count;
        command = "select count(s.id) from savings_potential_scenario s ";

        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);

        countQuery.setParameter("account_id", query.getOwnerId());
        if (!StringUtils.isBlank(query.getName())) {
            countQuery.setParameter("name", query.getName() + "%");
        }
        if (!query.getStatus().equals(EnumSavingScenarioStatus.UNDEFINED)) {
            countQuery.setParameter("status", query.getStatus());
        }

        count = countQuery.getSingleResult().intValue();

        // Load data
        command = "select s from savings_potential_scenario s ";

        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        switch (query.getSortBy()) {
            case NAME:
                command += " order by s.name ";
                break;
            case CREATED_ON:
                command += " order by s.createdOn ";
                break;
            case STATUS:
                command += " order by s.status ";
                break;
        }
        if (query.isSortAscending()) {
            command += " asc";
        } else {
            command += " desc";
        }

        TypedQuery<SavingsPotentialScenarioEntity> selectQuery = entityManager.createQuery(command, SavingsPotentialScenarioEntity.class);

        selectQuery.setParameter("account_id", query.getOwnerId());
        if (!StringUtils.isBlank(query.getName())) {
            selectQuery.setParameter("name", query.getName() + "%");
        }
        if (!query.getStatus().equals(EnumSavingScenarioStatus.UNDEFINED)) {
            selectQuery.setParameter("status", query.getStatus());
        }

        selectQuery.setFirstResult(query.getPageIndex() * query.getPageSize());
        selectQuery.setMaxResults(query.getPageSize());

        List<SavingsPotentialScenarioEntity> entities = selectQuery.getResultList();
        List<SavingScenario> scenarios = new ArrayList<SavingScenario>();
        for (SavingsPotentialScenarioEntity entity : entities) {
            try {
                scenarios.add(new SavingScenario(entity, objectMapper));
            } catch (IOException e) {
                throw createApplicationException(SavingsPotentialErrorCode.PARSE_ERROR);
            }
        }

        return new SavingScenarioQueryResult(query.getPageIndex(), query.getPageSize(), count, scenarios);
    }

    /**
     * Stores savings potential data to data store.
     *
     * @param scenarioId scenario id.
     * @param jobId job id
     * @param clusters the clusters.
     */
    @Override
    public void storeSavings(long scenarioId, long jobId, SavingsClusterCollection clusters) {
        SavingsPotentialScenarioEntity scenario = getScenarioById(scenarioId);
        scenario.setJobId(jobId);

        for (SavingsCluster cluster : clusters.clusters) {
            for (SavingsClusterMonth month : cluster.months.values()) {
                for (SavingsClusterMonth.Consumer consumer : month.consumers.values()) {
                    SavingsPotentialResultEntity entity = new SavingsPotentialResultEntity();
                    entity.setScenario(scenario);
                    entity.setCluster(cluster.name);
                    entity.setMonth(month.index);
                    entity.setSerial(consumer.serial);
                    entity.setSavingsPercent(month.percent);
                    entity.setSavingsVolume(month.volume);
                    entity.setClusterSize(month.consumers.size());
                    entity.setIq(consumer.waterIq);
                    entity.setDeviation(consumer.deviation);
                    entityManager.persist(entity);
                }
            }
        }
    }

    /**
     * Stores Water IQ data to data store.
     *
     * @param utilityId utility id
     * @param jobId job id
     * @param clusters the clusters.
     */
    @Override
    public void storeWaterIq(int utilityId, long jobId, SavingsClusterCollection clusters) {
        entityManager.createQuery("delete from savings_potential_water_iq").executeUpdate();

        UtilityEntity utility = getUtilityById(utilityId);

        for (SavingsCluster cluster : clusters.clusters) {
            for (SavingsClusterMonth month : cluster.months.values()) {
                for (SavingsClusterMonth.Consumer consumer : month.consumers.values()) {
                    SavingsPotentialWaterIqEntity entity = new SavingsPotentialWaterIqEntity();
                    entity.setUtility(utility);
                    entity.setJobId(jobId);
                    entity.setMonth(month.index);
                    entity.setSerial(consumer.serial);
                    entity.setIq(consumer.waterIq);
                    entityManager.persist(entity);
                }
            }
        }
    }

    /**
     * Resets status for all savings scenarios whose processing has been
     * interrupted.
     */
    @Override
    public void cleanStatus() {
        List<EnumSavingScenarioStatus> status = new ArrayList<EnumSavingScenarioStatus>();
        for (EnumSavingScenarioStatus value : EnumSavingScenarioStatus.values()) {
            if ((value != EnumSavingScenarioStatus.COMPLETED) && (value != EnumSavingScenarioStatus.FAILED)) {
                status.add(value);
            }
        }

        String queryString = "select s from savings_potential_scenario s where s.status in :status";

        TypedQuery<SavingsPotentialScenarioEntity> query = entityManager.createQuery(queryString, SavingsPotentialScenarioEntity.class)
                                                                        .setParameter("status", status);

        for(SavingsPotentialScenarioEntity scenario : query.getResultList()) {
            scenario.setStatus(EnumSavingScenarioStatus.ABANDONED);
        }
    }

    /**
     * Returns all the results for a specific scenario.
     *
     * @param key the scenario key.
     * @return a list of {@link SavingsPotentialResultEntity}.
     */
    @Override
    public List<SavingsPotentialResultEntity> getScenarioResults(UUID key) {
        String queryString = "SELECT r FROM savings_potential_result r where r.scenario.key = :key";

        TypedQuery<SavingsPotentialResultEntity> query = entityManager.createQuery(queryString, SavingsPotentialResultEntity.class)
                                                                      .setParameter("key", key);

        return query.getResultList();
    }

    /**
     * Compute savings potential per cluster segment for a given scenario and cluster.
     *
     * @param scenarioKey the scenario key.
     * @param clusterKey the cluster key.
     * @return a list of {@link SavingScenarioSegmentEntity}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SavingScenarioSegmentEntity> explore(UUID scenarioKey, UUID clusterKey) {
        String queryString = "select        g.id as id, " +
                             "              c.name as cluster_name, c.key as cluster_key, " +
                             "              g.name as segment_name, g.key as segment_key, " +
                             "              sum(a.savings_volume) as potential, sum(a.consumption) as consumption " +
                             "from          savings_potential_scenario s " +
                             "                  inner join savings_potential_account a on s.id = a.scenario_id " +
                             "                  inner join group_member m on a.account_id = m.account_id " +
                             "                  inner join group_segment gs on m.group_id = gs.id " +
                             "                  inner join \"cluster\" c on gs.cluster_id = c.id " +
                             "                  inner join \"group\" g on gs.id = g.id         " +
                             "where         s.key = cast(?1 as uuid) and " +
                             "              c.key = cast(?2 as uuid) " +
                             "group by      g.id, c.name, c.key, g.name, g.key " +
                             "order by      g.name";


        Query query = entityManager.createNativeQuery(queryString, "ScenarioClusterSegmentResult")
                                   .setParameter(1, scenarioKey)
                                   .setParameter(2, clusterKey);

        return (List<SavingScenarioSegmentEntity>) query.getResultList();
    }

    private UtilityEntity getUtilityById(int id) {
        String queryString = "SELECT u FROM utility u where u.id = :id";

        TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery(queryString, UtilityEntity.class)
                                                              .setParameter("id", id);

        return utilityQuery.getSingleResult();
    }

    private AccountEntity getAccountById(int id) {
        String queryString = "SELECT a FROM account a where a.id = :id";

        TypedQuery<AccountEntity> utilityQuery = entityManager.createQuery(queryString, AccountEntity.class)
                                                              .setParameter("id", id);

        return utilityQuery.getSingleResult();
    }
}
