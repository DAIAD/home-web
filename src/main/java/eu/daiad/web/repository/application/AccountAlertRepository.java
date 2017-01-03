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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AlertEntity;
import eu.daiad.web.model.message.Alert.ParameterizedTemplate;
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
    public Long countAll()
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a", Long.class);
        return query.getSingleResult();
    }
    
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
    public Long countByAccount(UUID accountKey)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE a.account.key = :accountKey",
            Long.class);
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult();
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
    public Long countByType(EnumAlertType alertType)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE a.alert.id = :aid", Long.class);
        query.setParameter("aid", alertType.getValue());
        return query.getSingleResult();
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
    public Long countByAccountAndType(UUID accountKey, EnumAlertType alertType)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE a.alert.id = :aid AND a.account.key = :accountKey",
            Long.class);
        query.setParameter("aid", alertType.getValue());
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult();
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
    public Long countByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
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
    public Long countByType(EnumAlertType alertType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE " +
                "a.alert.id = :aid AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("aid", alertType.getValue());
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
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
    public Long countByAccountAndType(UUID accountKey, EnumAlertType alertType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_alert a WHERE " +
                "a.alert.id = :aid AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("aid", alertType.getValue());
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult(); 
    }
    
    @Override
    public AccountAlertEntity findOne(int id)
    {
        return entityManager.find(AccountAlertEntity.class, id);
    }

    @Override
    public AccountAlertEntity create(AccountAlertEntity e)
    {
        e.setCreatedOn(DateTime.now());
        entityManager.persist(e);
        return e;
    }

    public AccountAlertEntity createWith(AccountEntity account, EnumAlertType alertType, Map<String, Object> p)
    {
        // Ensure we have a persistent AccountEntity instance
        if (!entityManager.contains(account)) 
            account = entityManager.find(AccountEntity.class, account.getId());
        
        AlertEntity alert = entityManager.find(AlertEntity.class, alertType.getValue());
        AccountAlertEntity e = new AccountAlertEntity(account, alert, p);
        return create(e);
    }

    @Override
    public AccountAlertEntity createWith(AccountEntity account, ParameterizedTemplate parameters)
    {
        return createWith(account, parameters.getType(), parameters.getParameters());
    }
    
    @Override
    public AccountAlertEntity createWith(UUID accountKey, ParameterizedTemplate parameters)
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
