package eu.daiad.web.repository.application;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AlertByTypeRecord;
import eu.daiad.web.domain.application.AlertTemplateEntity;
import eu.daiad.web.domain.application.AlertTemplateTranslationEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.EnumAlertTemplate;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class AccountAlertRepository extends BaseRepository
    implements IAccountAlertRepository
{
    public static final int DEFAULT_LIMIT = 50;

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    IAlertTemplateTranslationRepository translationRepository;

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

        query.setMaxResults(pagination.getLimit());

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
    public AccountAlertEntity create(AccountAlertEntity e)
    {
        e.setCreatedOn(DateTime.now());
        entityManager.persist(e);
        return e;
    }

    public AccountAlertEntity createWith(
        AccountEntity account, EnumAlertTemplate template, Map<String, Object> p)
    {
        // Ensure we have a persistent AccountEntity instance
        if (!entityManager.contains(account))
            account = entityManager.find(AccountEntity.class, account.getId());

        // Find entity mapping to target template
        AlertTemplateEntity templateEntity =
            entityManager.find(AlertTemplateEntity.class, template.getValue());

        AccountAlertEntity e =
            new AccountAlertEntity(account, templateEntity, p);
        return create(e);
    }

    @Override
    public AccountAlertEntity createWith(UUID accountKey, Alert.ParameterizedTemplate parameters)
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
        else
            return createWith(account, parameters);
    }

    @Override
    public AccountAlertEntity createWith(
        AccountEntity account, Alert.ParameterizedTemplate parameterizedTemplate)
    {
        return createWith(account, parameterizedTemplate.getTemplate(), parameterizedTemplate.getParameters());
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

        // Format

        // Todo: Some parameters need pre-processing (currencies, dates)
        Map<String, Object> parameters = r.getParametersAsMap();

        String title = (new MessageFormat(translation.getTitle(), locale))
            .format(parameters);

        String description = (new MessageFormat(translation.getDescription(), locale))
            .format(parameters);

        Alert message = new Alert(r.getId(), template);
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
