package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountStaticRecommendationEntity;
import eu.daiad.web.repository.BaseRepository;

@Repository 
@Transactional("applicationTransactionManager")
public class AccountStaticRecommendationRepository extends BaseRepository
    implements IAccountStaticRecommendationRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public AccountStaticRecommendationEntity findOne(int id)
    {
        return entityManager.find(AccountStaticRecommendationEntity.class, id);
    }

    @Override
    public List<AccountStaticRecommendationEntity> findByAccount(UUID accountKey)
    {
        TypedQuery<AccountStaticRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_static_recommendation a WHERE a.account.key = :accountKey",
            AccountStaticRecommendationEntity.class);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public List<AccountStaticRecommendationEntity> findByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<AccountStaticRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_static_recommendation a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
                AccountStaticRecommendationEntity.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public List<AccountStaticRecommendationEntity> findByType(int recommendationType)
    {
        TypedQuery<AccountStaticRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_static_recommendation a WHERE a.recommendation.id = :rid",
            AccountStaticRecommendationEntity.class);
        query.setParameter("rid", recommendationType);
        return query.getResultList();
    }

    @Override
    public List<AccountStaticRecommendationEntity> findByType(int recommendationType, Interval interval)
    {
        TypedQuery<AccountStaticRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_static_recommendation a WHERE " +
                "a.recommendation.id = :rid AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
                AccountStaticRecommendationEntity.class);
        query.setParameter("rid", recommendationType);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public List<AccountStaticRecommendationEntity> findByAccountAndType(UUID accountKey, int recommendationType)
    {
        TypedQuery<AccountStaticRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_static_recommendation a " +
                "WHERE a.recommendation.id = :rid AND a.account.key = :accountKey",
            AccountStaticRecommendationEntity.class);
        query.setParameter("rid", recommendationType);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public List<AccountStaticRecommendationEntity> findByAccountAndType(
        UUID accountKey, int recommendationType, Interval interval)
    {
        TypedQuery<AccountStaticRecommendationEntity> query = entityManager.createQuery(
            "SELECT a FROM account_static_recommendation a WHERE " +
                "a.recommendation.id = :rid AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountStaticRecommendationEntity.class);
        query.setParameter("rid", recommendationType);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public AccountStaticRecommendationEntity findLastForAccount(UUID accountKey)
    {
        TypedQuery<AccountStaticRecommendationEntity> q = entityManager.createQuery(
            "SELECT a FROM account_static_recommendation a " + 
                "WHERE a.account.key = :accountKey " +
                "ORDER by a.createdOn DESC",
            AccountStaticRecommendationEntity.class);
        q.setParameter("accountKey", accountKey);
        q.setMaxResults(1);
        
        AccountStaticRecommendationEntity e;
        try {
            e = q.getSingleResult();
        } catch (NoResultException ex) {
            e = null;
        }
        
        return e;
    }
}
