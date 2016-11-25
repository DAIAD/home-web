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
import eu.daiad.web.domain.application.AccountAnnouncementEntity;
import eu.daiad.web.repository.BaseRepository;

@Repository 
@Transactional("applicationTransactionManager")
public class AccountAnnouncementRepository extends BaseRepository
    implements IAccountAnnouncementRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public AccountAnnouncementEntity findOne(int id)
    {
        return entityManager.find(AccountAnnouncementEntity.class, id);
    }

    @Override
    public List<AccountAnnouncementEntity> findByAccount(UUID accountKey)
    {
        TypedQuery<AccountAnnouncementEntity> query = entityManager.createQuery(
            "SELECT a FROM account_announcement a WHERE a.account.key = :accountKey",
            AccountAnnouncementEntity.class);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public List<AccountAnnouncementEntity> findByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<AccountAnnouncementEntity> query = entityManager.createQuery(
            "SELECT a FROM account_announcement a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
             AccountAnnouncementEntity.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public List<AccountAnnouncementEntity> findByType(int announcementType)
    {
        TypedQuery<AccountAnnouncementEntity> query = entityManager.createQuery(
            "SELECT a FROM account_announcement a WHERE a.announcement.id = :aid",
            AccountAnnouncementEntity.class);
        query.setParameter("aid", announcementType);
        return query.getResultList();
    }

    @Override
    public List<AccountAnnouncementEntity> findByType(int announcementType, Interval interval)
    {
        TypedQuery<AccountAnnouncementEntity> query = entityManager.createQuery(
            "SELECT a FROM account_announcement a WHERE " +
                "a.announcement.id = :aid AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountAnnouncementEntity.class);
        query.setParameter("aid", announcementType);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

    @Override
    public List<AccountAnnouncementEntity> findByAccountAndType(UUID accountKey, int announcementType)
    {
        TypedQuery<AccountAnnouncementEntity> query = entityManager.createQuery(
            "SELECT a FROM account_announcement a WHERE " +
                "a.announcement.id = :aid AND a.account.key = :accountKey",
            AccountAnnouncementEntity.class);
        query.setParameter("aid", announcementType);
        query.setParameter("accountKey", accountKey);
        return query.getResultList();
    }

    @Override
    public List<AccountAnnouncementEntity> findByAccountAndType(
        UUID accountKey, int announcementType, Interval interval)
    {
        TypedQuery<AccountAnnouncementEntity> query = entityManager.createQuery(
            "SELECT a FROM account_announcement a WHERE " +
                "a.announcement.id = :aid AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            AccountAnnouncementEntity.class);
        query.setParameter("aid", announcementType);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getResultList();
    }

}
