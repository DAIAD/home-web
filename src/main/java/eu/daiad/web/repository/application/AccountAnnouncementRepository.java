package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountAnnouncementEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AnnouncementEntity;
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
    public Long countAll()
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_announcement", Long.class);
        return query.getSingleResult();
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
    public Long countByAccount(UUID accountKey)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_announcement a WHERE a.account.key = :accountKey",
            Long.class);
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult();
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
    public Long countByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_announcement a WHERE " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
             Long.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
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
    public Long countByType(int announcementType)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_announcement a WHERE a.announcement.id = :aid",
            Long.class);
        query.setParameter("aid", announcementType);
        return query.getSingleResult();
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
    public Long countByType(int announcementType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_announcement a WHERE " +
                "a.announcement.id = :aid AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("aid", announcementType);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
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
    public Long countByAccountAndType(UUID accountKey, int announcementType)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_announcement a WHERE " +
                "a.announcement.id = :aid AND a.account.key = :accountKey",
            Long.class);
        query.setParameter("aid", announcementType);
        query.setParameter("accountKey", accountKey);
        return query.getSingleResult();
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

    @Override
    public Long countByAccountAndType(UUID accountKey, int announcementType, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_announcement a WHERE " +
                "a.announcement.id = :aid AND " +
                "a.account.key = :accountKey AND " +
                "a.createdOn >= :start AND a.createdOn < :end",
            Long.class);
        query.setParameter("aid", announcementType);
        query.setParameter("accountKey", accountKey);
        query.setParameter("start", interval.getStart());
        query.setParameter("end", interval.getEnd());
        return query.getSingleResult();
    }

    @Override
    public AccountAnnouncementEntity create(AccountAnnouncementEntity e)
    {
        e.setCreatedOn(DateTime.now());
        entityManager.persist(e);
        return e;
    }

    @Override
    public AccountAnnouncementEntity createWith(AccountEntity account, int announcementType)
    {
        // Ensure we have a persistent AccountEntity instance
        if (!entityManager.contains(account))
            account = entityManager.find(AccountEntity.class, account.getId());

        AnnouncementEntity announcement =
            entityManager.find(AnnouncementEntity.class, announcementType);
        AccountAnnouncementEntity e =
            new AccountAnnouncementEntity(account, announcement);
        return create(e);
    }

    @Override
    public AccountAnnouncementEntity createWith(UUID accountKey, int announcementType)
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
            return createWith(account, announcementType);
    }

    @Override
    public void delete(int id)
    {
        AccountAnnouncementEntity e =
            entityManager.find(AccountAnnouncementEntity.class, id);
        if (e != null)
            delete(e);
    }

    @Override
    public void delete(AccountAnnouncementEntity e)
    {
        entityManager.remove(e);
    }
}
