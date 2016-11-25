package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.repository.BaseRepository;

@Repository 
@Transactional("applicationTransactionManager")
public class AccountAlertRepository extends BaseRepository 
    implements IAccountAlertRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public List<AccountAlertEntity> findByAccount(UUID accountKey)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE a.account.key = :accountKey",
            AccountAlertEntity.class);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public List<AccountAlertEntity> findByType(EnumAlertType alertType)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE a.alert.id = :aid",
            AccountAlertEntity.class);
        query.setParameter("aid", alertType.getValue());
        return query.getResultList(); 
    }

    @Override
    public List<AccountAlertEntity> findByAccountAndType(UUID accountKey, EnumAlertType alertType)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE a.alert.id = :aid AND a.account.key = :accountKey",
            AccountAlertEntity.class);
        query.setParameter("aid", alertType.getValue());
        query.setParameter("accountKey", accountKey);
        return query.getResultList(); 
    }

    @Override
    public List<AccountAlertEntity> findByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountAlertEntity.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public List<AccountAlertEntity> findByType(EnumAlertType alertType, Interval interval)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE " +
                "a.alert.id = :aid AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountAlertEntity.class);
        query.setParameter("aid", alertType.getValue());
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList(); 
    }

    @Override
    public List<AccountAlertEntity> findByAccountAndType(
        UUID accountKey, EnumAlertType alertType, Interval interval)
    {
        TypedQuery<AccountAlertEntity> query = entityManager.createQuery(
            "SELECT a FROM account_alert a WHERE " +
                "a.alert.id = :aid AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountAlertEntity.class);
        query.setParameter("aid", alertType.getValue());
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList(); 
    }

    @Override
    public AccountAlertEntity findOne(int id)
    {
        return entityManager.find(AccountAlertEntity.class, id);
    }
}
