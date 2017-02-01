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

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountRecommendationEntity;
import eu.daiad.web.domain.application.RecommendationByTypeRecord;
import eu.daiad.web.domain.application.RecommendationTemplateEntity;
import eu.daiad.web.domain.application.RecommendationTemplateTranslationEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.EnumRecommendationType;
import eu.daiad.web.model.message.Recommendation;
import eu.daiad.web.model.message.Recommendation.ParameterizedTemplate;
import eu.daiad.web.repository.BaseRepository;
import eu.daiad.web.service.ICurrencyRateService;

@Repository
@Transactional("applicationTransactionManager")
public class AccountRecommendationRepository extends BaseRepository
    implements IAccountRecommendationRepository
{
    public static final int DEFAULT_LIMIT = 50;

    private static final Log logger = LogFactory.getLog(AccountRecommendationRepository.class);
    
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    IRecommendationTemplateTranslationRepository translationRepository;

    @Autowired
    ICurrencyRateService currencyRateService;
    
    @Override
    public int countAll()
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a", Long.class);
        return query.getSingleResult().intValue();
    }

    @Override
    public List<AccountRecommendationEntity> findByAccount(UUID accountKey)
    {
        return findByAccount(accountKey, (Interval) null);
    }

    @Override
    public int countByAccount(UUID accountKey)
    {
        return countByAccount(accountKey, (Interval) null);
    }

    @Override
    public List<AccountRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType)
    {
        return findByAccountAndType(accountKey, recommendationType, (Interval) null);
    }

    @Override
    public int countByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType)
    {
        return countByAccountAndType(accountKey, recommendationType, (Interval) null);
    }

    @Override
    public List<AccountRecommendationEntity> findByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a WHERE " +
                "a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            AccountRecommendationEntity.class);

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
            "SELECT count(a.id) FROM account_recommendation a WHERE " +
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
    public List<AccountRecommendationEntity> findByAccount(UUID accountKey, int minId)
    {
        return findByAccount(accountKey, minId, new PagingOptions(DEFAULT_LIMIT));
    }

    @Override
    public List<AccountRecommendationEntity> findByAccount(UUID accountKey, int minId, PagingOptions pagination)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT r FROM account_recommendation r " +
                "WHERE r.account.key = :accountKey AND r.id > :minId " +
                "ORDER BY r.id " + (pagination.isAscending()? "ASC" : "DESC"),
            AccountRecommendationEntity.class);

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
            "SELECT count(r.id) FROM account_recommendation r " +
                "WHERE r.account.key = :accountKey AND r.id > :minId ",
            Long.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("minId", minId);
        return query.getSingleResult().intValue();
    }

    @Override
    public List<AccountRecommendationEntity> findByType(EnumRecommendationType recommendationType, UUID utilityKey)
    {
        return findByType(recommendationType, utilityKey, null);
    }

    @Override
    public List<AccountRecommendationEntity> findByType(
        EnumRecommendationType recommendationType, UUID utilityKey, Interval interval)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a WHERE " +
                "a.account.utility.key = :utilityKey " +
                "AND a.recommendationTemplate.type.value = :rtype " +
                ((interval != null)? "AND a.createdOn >= :start AND a.createdOn < :end " : ""),
            AccountRecommendationEntity.class);

        query.setParameter("utilityKey", utilityKey);
        query.setParameter("rtype", recommendationType.getValue());

        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getResultList();
    }

    @Override
    public int countByType(EnumRecommendationType recommendationType, UUID utilityKey)
    {
        return countByType(recommendationType, utilityKey, null);
    }

    @Override
    public int countByType(
        EnumRecommendationType recommendationType, UUID utilityKey, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a WHERE " +
                "a.account.utility.key = :utilityKey " +
                "AND a.recommendationTemplate.type.value = :rtype " +
                ((interval != null)? "AND a.createdOn >= :start AND a.createdOn < :end " : ""),
            Long.class);

        query.setParameter("utilityKey", utilityKey);
        query.setParameter("rtype", recommendationType.getValue());

        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getSingleResult().intValue();
    }

    @Override
    public Map<EnumRecommendationType, Integer> countByType(UUID utilityKey)
    {
        return countByType(utilityKey, null);
    }

    @Override
    public Map<EnumRecommendationType, Integer> countByType(UUID utilityKey, Interval interval)
    {
        Map<EnumRecommendationType, Integer> r = new EnumMap<>(EnumRecommendationType.class);

        TypedQuery<RecommendationByTypeRecord> query = entityManager.createQuery(
            "SELECT new eu.daiad.web.domain.application.RecommendationByTypeRecord(" +
                    "a.recommendationTemplate.type.value, count(a.id)) " +
                "FROM account_recommendation a " +
                "WHERE " +
                    "a.account.utility.key = :utilityKey " +
                    ((interval != null)? "AND a.createdOn >= :start AND a.createdOn < :end " : "") +
                "GROUP BY a.recommendationTemplate.type.value",
                RecommendationByTypeRecord.class);

        query.setParameter("utilityKey", utilityKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        for (RecommendationByTypeRecord counter: query.getResultList())
            r.put(counter.getType(), counter.getCount());

        return r;
    }

    @Override
    public List<AccountRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a WHERE " +
                "a.recommendationTemplate.type.value = :rtype" +
                " AND a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            AccountRecommendationEntity.class);

        query.setParameter("rtype", recommendationType.getValue());
        query.setParameter("accountKey", accountKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getResultList();
    }

    @Override
    public int countByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a WHERE " +
                "a.recommendationTemplate.type.value = :rtype" +
                " AND a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            Long.class);

        query.setParameter("rtype", recommendationType.getValue());
        query.setParameter("accountKey", accountKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }

        return query.getSingleResult().intValue();
    }

    @Override
    public AccountRecommendationEntity findOne(int id)
    {
        return entityManager.find(AccountRecommendationEntity.class, id);
    }

    @Override
    public AccountRecommendationEntity create(AccountRecommendationEntity e)
    {
        e.setCreatedOn(DateTime.now());
        entityManager.persist(e);
        return e;
    }

    @Override
    public AccountRecommendationEntity createWith(UUID accountKey, ParameterizedTemplate parameters)
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
    public AccountRecommendationEntity createWith(
        AccountEntity account, ParameterizedTemplate parameterizedTemplate)
    {
        // Ensure we have a persistent AccountEntity instance
        if (!entityManager.contains(account))
            account = entityManager.find(AccountEntity.class, account.getId());

        // Find entity mapping to target template
        
        EnumRecommendationTemplate template = parameterizedTemplate.getTemplate();
        RecommendationTemplateEntity templateEntity =
            entityManager.find(RecommendationTemplateEntity.class, template.getValue());
        
        // Create
        
        AccountRecommendationEntity r = 
            new AccountRecommendationEntity(account, templateEntity, parameterizedTemplate);
        return create(r);
    }

    @Override
    public void delete(int id)
    {
        AccountRecommendationEntity r = findOne(id);
        if (r != null)
            delete(r);
    }

    @Override
    public void delete(AccountRecommendationEntity r)
    {
        if (!entityManager.contains(r))
            r = findOne(r.getId());
        if (r != null)
            entityManager.remove(r);
    }

    @Override
    public boolean acknowledge(int id, DateTime acknowledged)
    {
        AccountRecommendationEntity r = findOne(id);
        if (r != null)
            return acknowledge(r, acknowledged);
        return false;
    }

    @Override
    public boolean acknowledge(UUID accountKey, int id, DateTime acknowledged)
    {
        AccountRecommendationEntity r = findOne(id);
        // Perform action only if message exists and is owned by account
        if (r != null && r.getAccount().getKey().equals(accountKey))
            return acknowledge(r, acknowledged);
        return false;
    }

    @Override
    public boolean acknowledge(AccountRecommendationEntity r, DateTime acknowledged)
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
    public Recommendation formatMessage(AccountRecommendationEntity r, Locale locale)
    {
        if (!entityManager.contains(r))
            r = findOne(r.getId());
        if (r == null)
            return null;

        // Find a proper translated template

        EnumRecommendationTemplate template = r.getTemplate().asEnum();

        RecommendationTemplateTranslationEntity translation = null;
        translation = translationRepository.findByTemplate(template, locale);
        if (translation == null)
            translation = translationRepository.findByTemplate(template);
        if (translation == null)
            return null;

        // Retrieve generation-time parameters: rebuild a parameterized template

        ParameterizedTemplate parameterizedTemplate = null; 
        try {
            parameterizedTemplate = r.getParameters().toParameterizedTemplate();
        } catch (ClassCastException | ClassNotFoundException | IOException ex) {
            logger.error(String.format(
                "Failed to re-build parameterized template for recommendation#%d: %s",
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
        
        Recommendation message = new Recommendation(r.getId(), template);
        message.setLocale(locale.getLanguage());
        message.setTitle(title);
        message.setDescription(description);
        message.setImageLink(translation.getImageLink());
        message.setCreatedOn(r.getCreatedOn());
        if (r.getAcknowledgedOn() != null)
            message.setAcknowledgedOn(r.getAcknowledgedOn());

        return message;
    }

    @Override
    public Recommendation formatMessage(int id, Locale locale)
    {
        AccountRecommendationEntity r = findOne(id);
        if (r != null)
            return formatMessage(r, locale);
        return null;
    }
}
