package eu.daiad.web.repository.application;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AlertByTypeRecord;
import eu.daiad.web.domain.application.AlertResolverExecutionEntity;
import eu.daiad.web.domain.application.AlertTemplateEntity;
import eu.daiad.web.domain.application.AlertTemplateTranslationEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.Alert.ParameterizedTemplate;
import eu.daiad.web.model.message.EnumAlertTemplate;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.repository.BaseRepository;
import eu.daiad.web.service.ICurrencyRateService;

@Repository
@Transactional("applicationTransactionManager")
public class AccountAlertRepository extends BaseRepository
    implements IAccountAlertRepository
{
    public static final int DEFAULT_LIMIT = 50;

    private static final Log logger = LogFactory.getLog(AccountAlertRepository.class);
    
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    IAlertTemplateTranslationRepository translationRepository;
    
    @Autowired
    ICurrencyRateService currencyRateService;
    
    @Override
    public AccountAlertEntity findOne(int id)
    {
        return entityManager.find(AccountAlertEntity.class, id);
    }

    @Override
    public int countAll()
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a", Long.class);
        return query.getSingleResult().intValue();
    }

    @Override
    public List<AccountAlertEntity> findByAccount(UUID accountKey)
    {
        return findByAccount(accountKey, (Interval) null);
    }

    @Override
    public int countByAccount(UUID accountKey)
    {
        return countByAccount(accountKey, (Interval) null);
    }

    @Override
    public List<AccountAlertEntity> findByAccountAndType(
        UUID accountKey, EnumAlertType alertType)
    {
        return findByAccountAndType(accountKey, alertType, (Interval) null);
    }

    @Override
    public int countByAccountAndType(
        UUID accountKey, EnumAlertType alertType)
    {
        return countByAccountAndType(accountKey, alertType, (Interval) null);
    }

    @Override
    public List<AccountAlertEntity> findByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE " +
                "a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            AccountAlertEntity.class);

        query.setParameter("accountKey", accountKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getResultList();
    }

    @Override
    public int countByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE " +
                "a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            Long.class);

        query.setParameter("accountKey", accountKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getSingleResult().intValue();
    }

    @Override
    public List<AccountAlertEntity> findByAccount(UUID accountKey, int minId)
    {
        return findByAccount(accountKey, minId, new PagingOptions(DEFAULT_LIMIT));
    }

    @Override
    public List<AccountAlertEntity> findByAccount(UUID accountKey, int minId, PagingOptions pagination)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT r FROM account_alert r " +
                "WHERE r.account.key = :accountKey AND r.id > :minId " +
                "ORDER BY r.id " + (pagination.isAscending()? "ASC" : "DESC"),
            AccountAlertEntity.class);

        query.setParameter("accountKey", accountKey);
        query.setParameter("minId", minId);

        int offset = pagination.getOffset();
        if (offset > 0)
            query.setFirstResult(offset);

        query.setMaxResults(pagination.getSize());

        return query.getResultList();
    }

    @Override
    public int countByAccount(UUID accountKey, int minId)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(r.id) FROM account_alert r " +
                "WHERE r.account.key = :accountKey AND r.id > :minId ",
            Long.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("minId", minId);
        return query.getSingleResult().intValue();
    }

    @Override
    public List<AccountAlertEntity> findByType(EnumAlertType alertType, UUID utilityKey)
    {
        return findByType(alertType, utilityKey, null);
    }

    @Override
    public List<AccountAlertEntity> findByType(
        EnumAlertType alertType, UUID utilityKey, Interval interval)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE " +
                "a.account.utility.key = :utilityKey " +
                "AND a.alertTemplate.type.value = :rtype " +
                ((interval != null)? "AND a.createdOn >= :start AND a.createdOn < :end " : ""),
            AccountAlertEntity.class);

        query.setParameter("utilityKey", utilityKey);
        query.setParameter("rtype", alertType.getValue());

        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getResultList();
    }

    @Override
    public int countByType(EnumAlertType alertType, UUID utilityKey)
    {
        return countByType(alertType, utilityKey, null);
    }

    @Override
    public int countByType(
        EnumAlertType alertType, UUID utilityKey, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE " +
                "a.account.utility.key = :utilityKey " +
                "AND a.alertTemplate.type.value = :rtype " +
                ((interval != null)? "AND a.createdOn >= :start AND a.createdOn < :end " : ""),
            Long.class);

        query.setParameter("utilityKey", utilityKey);
        query.setParameter("rtype", alertType.getValue());

        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getSingleResult().intValue();
    }

    @Override
    public Map<EnumAlertType, Integer> countByType(UUID utilityKey)
    {
        return countByType(utilityKey, null);
    }

    @Override
    public Map<EnumAlertType, Integer> countByType(UUID utilityKey, Interval interval)
    {
        Map<EnumAlertType, Integer> r = new EnumMap<>(EnumAlertType.class);

        TypedQuery<AlertByTypeRecord> query = entityManager.createQuery(
            "SELECT new eu.daiad.web.domain.application.AlertByTypeRecord(" +
                    "a.alertTemplate.type.value, count(a.id)) " +
                "FROM account_alert a " +
                "WHERE " +
                    "a.account.utility.key = :utilityKey " +
                    ((interval != null)? "AND a.createdOn >= :start AND a.createdOn < :end " : "") +
                "GROUP BY a.alertTemplate.type.value",
                AlertByTypeRecord.class);

        query.setParameter("utilityKey", utilityKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        for (AlertByTypeRecord counter: query.getResultList())
            r.put(counter.getType(), counter.getCount());

        return r;
    }

    @Override
    public List<AccountAlertEntity> findByAccountAndType(
        UUID accountKey, EnumAlertType alertType, Interval interval)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE " +
                "a.alertTemplate.type.value = :rtype" +
                " AND a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            AccountAlertEntity.class);

        query.setParameter("rtype", alertType.getValue());
        query.setParameter("accountKey", accountKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getResultList();
    }

    @Override
    public int countByAccountAndType(
        UUID accountKey, EnumAlertType alertType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE " +
                "a.alertTemplate.type.value = :rtype" +
                " AND a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            Long.class);

        query.setParameter("rtype", alertType.getValue());
        query.setParameter("accountKey", accountKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getSingleResult().intValue();
    }

    @Override
    public List<AccountAlertEntity> findByExecution(int rid)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE a.resolver_execution.id = :rid",
            AccountAlertEntity.class);
        query.setParameter("rid", rid);
        return query.getResultList();
    }

    @Override
    public List<AccountAlertEntity> findByExecution(List<Integer> rids)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE a.resolver_execution.id IN (:rids)",
            AccountAlertEntity.class);
        query.setParameter("rids", rids);
        return query.getResultList();
    }

    @Override
    public List<AccountAlertEntity> findByAccountAndExecution(UUID accountKey, int rid)
    { 
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a " +
                "WHERE a.resolver_execution.id = :rid AND a.account.key = :accountKey",
            AccountAlertEntity.class);
        query.setParameter("rid", rid);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public List<AccountAlertEntity> findByAccountAndExecution(UUID accountKey, List<Integer> rids)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a " +
                "WHERE a.resolver_execution.id IN (:rids) AND a.account.key = :accountKey",
            AccountAlertEntity.class);
        query.setParameter("rids", rids);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public int countByExecution(int rid)
    {
        TypedQuery<Integer> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE a.resolver_execution.id = :rid",
            Integer.class);
        query.setParameter("rid", rid);
        return query.getSingleResult().intValue();
    }

    @Override
    public int countByExecution(List<Integer> rids)
    {
        TypedQuery<Integer> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE a.resolver_execution.id IN (:rids)",
            Integer.class);
        query.setParameter("rids", rids);
        return query.getSingleResult().intValue();
    }

    @Override
    public int countByAccountAndExecution(UUID accountKey, int rid)
    {
        TypedQuery<Integer> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a " +
                "WHERE a.resolver_execution.id = :rid AND a.account.key = :accountKey",
            Integer.class);
        query.setParameter("rid", rid);
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult().intValue();
    }

    @Override
    public int countByAccountAndExecution(UUID accountKey, List<Integer> rids)
    {
        TypedQuery<Integer> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a " +
                "WHERE a.resolver_execution.id IN (:rids) AND a.account.key = :accountKey",
            Integer.class);
        query.setParameter("rids", rids);
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult().intValue();
    }
    
    @Override
    public AccountAlertEntity create(AccountAlertEntity e)
    {
        e.setCreatedOn(DateTime.now());
        entityManager.persist(e);
        return e;
    }

    @Override
    public AccountAlertEntity createWith(
        UUID accountKey,
        ParameterizedTemplate parameterizedTemplate,
        AlertResolverExecutionEntity resolverExecution,
        EnumDeviceType deviceType)
    {
        TypedQuery<AccountEntity> query = entityManager.createQuery(
            "SELECT a FROM account a WHERE a.key = :accountKey", AccountEntity.class);
        query.setParameter("accountKey", accountKey);

        AccountEntity account;
        try {
            account = query.getSingleResult();
        } catch (NoResultException x) {
            account = null;
        }

        if (account == null)
            return null;

        return createWith(account, parameterizedTemplate, resolverExecution, deviceType);
    }

    @Override
    public AccountAlertEntity createWith(
        AccountEntity account, 
        ParameterizedTemplate parameterizedTemplate,
        AlertResolverExecutionEntity resolverExecution,
        EnumDeviceType deviceType)
    {
        // Ensure we have a persistent AccountEntity instance
        if (!entityManager.contains(account))
            account = entityManager.find(AccountEntity.class, account.getId());
        
        // Find entity mapping to target template
        
        EnumAlertTemplate template = parameterizedTemplate.getTemplate();
        AlertTemplateEntity templateEntity =
            entityManager.find(AlertTemplateEntity.class, template.getValue());
        
        // Create
        
        AccountAlertEntity r = new AccountAlertEntity();
        r.setAccount(account);
        r.setTemplate(templateEntity);
        r.setParameters(parameterizedTemplate);
        r.setDeviceType(deviceType);
        r.setResolverExecution(resolverExecution);
        
        return create(r);
    }

    @Override
    public boolean acknowledge(int id, DateTime acknowledged)
    {
        AccountAlertEntity r = findOne(id);
        if (r != null)
            return acknowledge(r, acknowledged);
        return false;
    }

    @Override
    public boolean acknowledge(UUID accountKey, int id, DateTime acknowledged)
    {
        AccountAlertEntity r = findOne(id);
        // Perform action only if message exists and is owned by account
        if (r != null && r.getAccount().getKey().equals(accountKey))
            return acknowledge(r, acknowledged);
        return false;
    }

    @Override
    public boolean acknowledge(AccountAlertEntity r, DateTime acknowledged)
    {
        if (!entityManager.contains(r))
            r = findOne(r.getId());

        if (r != null && r.getAcknowledgedOn() == null) {
            r.setAcknowledgedOn(acknowledged);
            r.setReceiveAcknowledgedOn(new DateTime());
            return true;
        }
        return false;
    }

    @Override
    public Alert formatMessage(AccountAlertEntity r, Locale locale)
    {
        if (!entityManager.contains(r))
            r = findOne(r.getId());
        if (r == null)
            return null;

        // Find a proper translated template

        EnumAlertTemplate template = r.getTemplate().asEnum();

        AlertTemplateTranslationEntity translation = null;
        translation = translationRepository.findByTemplate(template, locale);
        if (translation == null)
            translation = translationRepository.findByTemplate(template);
        if (translation == null)
            return null;

        // Retrieve generation-time parameters (as a parameterized template)

        ParameterizedTemplate parameterizedTemplate = null; 
        try {
            parameterizedTemplate = r.getParameters().toParameterizedTemplate();
        } catch (ClassCastException | ClassNotFoundException | IOException ex) {
            logger.error(String.format(
                "Failed to retrieve parameterized template for alert#%d: %s",
                r.getId(), ex.getMessage()));
            parameterizedTemplate = null;
        }
        if (parameterizedTemplate == null)
            return null;
        
        // Make underlying parameters aware of target locale
        
        parameterizedTemplate = parameterizedTemplate.withLocale(locale, currencyRateService);
        
        // Format
        
        Map<String, Object> parameters = parameterizedTemplate.getParameters();
        
        String title = (new MessageFormat(translation.getTitle(), locale))
            .format(parameters);

        String description = (new MessageFormat(translation.getDescription(), locale))
            .format(parameters);
        
        // Build a DTO object with formatted messages
        
        Alert message = new Alert(r.getId(), template);
        message.setLocale(locale.getLanguage());
        message.setTitle(title);
        message.setDescription(description);
        message.setLink(translation.getLink());
        message.setCreatedOn(r.getCreatedOn());
        if (r.getAcknowledgedOn() != null)
            message.setAcknowledgedOn(r.getAcknowledgedOn());

        return message;
    }

    @Override
    public Alert formatMessage(int id, Locale locale)
    {
        AccountAlertEntity r = findOne(id);
        if (r != null)
            return formatMessage(r, locale);
        return null;
    }

    @Override
    public void delete(int id)
    {
        AccountAlertEntity e = entityManager.find(AccountAlertEntity.class, id);
        if (e != null)
            delete(e);
    }

    @Override
    public void delete(AccountAlertEntity e)
    {
        entityManager.remove(e);
    }
}
