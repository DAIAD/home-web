package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.repository.BaseRepository;
import eu.daiad.web.domain.application.AccountDynamicRecommendationEntity;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;

@Repository 
@Transactional("applicationTransactionManager")
public class AccountDynamicRecommendationRepository extends BaseRepository
    implements IAccountDynamicRecommendationRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
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
    public AccountDynamicRecommendationEntity findOne(int id)
    {
        return entityManager.find(AccountDynamicRecommendationEntity.class, id);
    }

}
