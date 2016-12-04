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
import eu.daiad.web.domain.application.AccountDynamicRecommendationEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.DynamicRecommendationEntity;
import eu.daiad.web.model.message.DynamicRecommendation.Parameters;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;

@Repository 
@Transactional("applicationTransactionManager")
public class AccountDynamicRecommendationRepository extends BaseRepository
    implements IAccountDynamicRecommendationRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public Long countAll()
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_dynamic_recommendation a", Long.class);
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountDynamicRecommendationEntity> findByAccount(UUID accountKey)
    {
        TypedQuery<AccountDynamicRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_dynamic_recommendation a WHERE a.account.key = :accountKey",
            AccountDynamicRecommendationEntity.class);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public Long countByAccount(UUID accountKey)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_dynamic_recommendation a WHERE a.account.key = :accountKey",
            Long.class);
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountDynamicRecommendationEntity> findByType(
        EnumDynamicRecommendationType recommendationType)
    {
        TypedQuery<AccountDynamicRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_dynamic_recommendation a WHERE a.recommendation.id = :rid",
            AccountDynamicRecommendationEntity.class);
        query.setParameter("rid", recommendationType.getValue());
        return query.getResultList();
    }

    @Override
    public Long countByType(EnumDynamicRecommendationType recommendationType)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_dynamic_recommendation a WHERE a.recommendation.id = :rid",
            Long.class);
        query.setParameter("rid", recommendationType.getValue());
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountDynamicRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType)
    {
        TypedQuery<AccountDynamicRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_dynamic_recommendation a " +
                "WHERE a.recommendation.id = :rid AND a.account.key = :accountKey",
            AccountDynamicRecommendationEntity.class);
        query.setParameter("rid", recommendationType.getValue());
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public Long countByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_dynamic_recommendation a " +
                "WHERE a.recommendation.id = :rid AND a.account.key = :accountKey",
            Long.class);
        query.setParameter("rid", recommendationType.getValue());
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountDynamicRecommendationEntity> findByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<AccountDynamicRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_dynamic_recommendation a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountDynamicRecommendationEntity.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public Long countByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_dynamic_recommendation a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountDynamicRecommendationEntity> findByType(
        EnumDynamicRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<AccountDynamicRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_dynamic_recommendation a WHERE " +
                "a.recommendation.id = :rid AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountDynamicRecommendationEntity.class);
        query.setParameter("rid", recommendationType.getValue());
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }
    
    @Override
    public Long countByType(EnumDynamicRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_dynamic_recommendation a WHERE " +
                "a.recommendation.id = :rid AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("rid", recommendationType.getValue());
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
    }
    
    @Override
    public List<AccountDynamicRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<AccountDynamicRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_dynamic_recommendation a WHERE " +
                "a.recommendation.id = :rid AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountDynamicRecommendationEntity.class);
        query.setParameter("rid", recommendationType.getValue());
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public Long countByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a) FROM account_dynamic_recommendation a WHERE " +
                "a.recommendation.id = :rid AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("rid", recommendationType.getValue());
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
    }
    
    @Override
    public AccountDynamicRecommendationEntity findOne(int id)
    {
        return entityManager.find(AccountDynamicRecommendationEntity.class, id);
    }

    @Override
    public AccountDynamicRecommendationEntity create(AccountDynamicRecommendationEntity e)
    {
        e.setCreatedOn(DateTime.now());
        entityManager.persist(e);
        return e;
    }

    public AccountDynamicRecommendationEntity createWith(
        AccountEntity account, EnumDynamicRecommendationType recommendationType, Map<String, Object> p)
    {
        // Ensure we have a persistent AccountEntity instance
        if (!entityManager.contains(account)) 
            account = entityManager.find(AccountEntity.class, account.getId());
        
        DynamicRecommendationEntity recommendation = 
            entityManager.find(DynamicRecommendationEntity.class, recommendationType.getValue());
        AccountDynamicRecommendationEntity e = 
            new AccountDynamicRecommendationEntity(account, recommendation, p);
        return create(e);
    }

    @Override
    public AccountDynamicRecommendationEntity createWith(UUID accountKey, Parameters parameters)
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
    public AccountDynamicRecommendationEntity createWith(
        AccountEntity account, Parameters parameters)
    {
        return createWith(account, parameters.getType(), parameters.getPairs());
    }
    
    @Override
    public void delete(int id)
    {
        AccountDynamicRecommendationEntity e = 
            entityManager.find(AccountDynamicRecommendationEntity.class, id);
        if (e != null)
            delete(e);
    }

    @Override
    public void delete(AccountDynamicRecommendationEntity e)
    {
        entityManager.remove(e);
    }
}
