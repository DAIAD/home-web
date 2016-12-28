package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.repository.BaseRepository;
import eu.daiad.web.domain.application.AccountRecommendationEntity;
import eu.daiad.web.domain.application.RecommendationMessageEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.RecommendationTypeEntity;
import eu.daiad.web.model.message.Recommendation.Parameters;
import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.EnumRecommendationType;

@Repository 
@Transactional("applicationTransactionManager")
public class AccountRecommendationRepository extends BaseRepository
    implements IAccountRecommendationRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public Long countAll()
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a", Long.class);
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountRecommendationEntity> findByAccount(UUID accountKey)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a WHERE a.account.key = :accountKey",
            AccountRecommendationEntity.class);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public Long countByAccount(UUID accountKey)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a WHERE a.account.key = :accountKey",
            Long.class);
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountRecommendationEntity> findByType(EnumRecommendationType recommendationType)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a WHERE a.recommendation.type = :rtype",
            AccountRecommendationEntity.class);
        query.setParameter("rtype", recommendationType.name());
        return query.getResultList();
    }

    @Override
    public Long countByType(EnumRecommendationType recommendationType)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a WHERE a.recommendation.type = :rtype",
            Long.class);
        query.setParameter("rtype", recommendationType.name());
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a " +
                "WHERE a.recommendation.type = :rtype AND a.account.key = :accountKey",
            AccountRecommendationEntity.class);
        query.setParameter("rtype", recommendationType.name());
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public Long countByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a " +
                "WHERE a.recommendation.type = :rtype AND a.account.key = :accountKey",
            Long.class);
        query.setParameter("rtype", recommendationType.name());
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountRecommendationEntity> findByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountRecommendationEntity.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public Long countByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountRecommendationEntity> findByType(
        EnumRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a WHERE " +
                "a.recommendation.type = :rtype AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountRecommendationEntity.class);
        query.setParameter("rtype", recommendationType.name());
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }
    
    @Override
    public Long countByType(EnumRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_recommendation a WHERE " +
                "a.recommendation.type = :rtype AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("rtype", recommendationType.name());
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<AccountRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_recommendation a WHERE " +
                "a.recommendation.type = :rtype AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountRecommendationEntity.class);
        query.setParameter("rtype", recommendationType.name());
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public Long countByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a) FROM account_recommendation a WHERE " +
                "a.recommendation.type = :rtype AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("rtype", recommendationType.name());
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
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

    public AccountRecommendationEntity createWith(
        AccountEntity account, EnumRecommendationTemplate template, Map<String, Object> p)
    {
        // Ensure we have a persistent AccountEntity instance
        if (!entityManager.contains(account)) 
            account = entityManager.find(AccountEntity.class, account.getId());

        AccountRecommendationEntity e = 
            new AccountRecommendationEntity(account, template, p);
        return create(e);
    }

    @Override
    public AccountRecommendationEntity createWith(UUID accountKey, Parameters parameters)
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
        AccountEntity account, Parameters parameters)
    {
        return createWith(account, parameters.getType(), parameters.getPairs());
    }
    
    @Override
    public void delete(int id)
    {
        AccountRecommendationEntity e = 
            entityManager.find(AccountRecommendationEntity.class, id);
        if (e != null)
            delete(e);
    }

    @Override
    public void delete(AccountRecommendationEntity e)
    {
        entityManager.remove(e);
    }
}
