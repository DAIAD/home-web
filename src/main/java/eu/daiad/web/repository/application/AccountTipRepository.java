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

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountTipEntity;
import eu.daiad.web.domain.application.TipEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.message.Tip;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class AccountTipRepository extends BaseRepository
    implements IAccountTipRepository
{
    public static final int DEFAULT_LIMIT = 20;

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public int countAll()
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_tip", Long.class);
        return query.getSingleResult().intValue();
    }

    @Override
    public AccountTipEntity findOne(int id)
    {
        return entityManager.find(AccountTipEntity.class, id);
    }

    @Override
    public List<AccountTipEntity> findByAccount(UUID accountKey)
    {
        return findByAccount(accountKey, (Interval) null);
    }

    @Override
    public int countByAccount(UUID accountKey)
    {
        return countByAccount(accountKey, (Interval) null);
    }

    @Override
    public List<AccountTipEntity> findByAccount(UUID accountKey, Interval interval)
    {
        TypedQuery<AccountTipEntity> query = entityManager.createQuery(
            "SELECT a FROM account_tip a WHERE " +
                "a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            AccountTipEntity.class);

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
            "SELECT count(a.id) FROM account_tip a WHERE " +
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
    public List<AccountTipEntity> findByAccount(UUID accountKey, int minId)
    {
        return findByAccount(accountKey, minId, new PagingOptions(DEFAULT_LIMIT));
    }

    @Override
    public List<AccountTipEntity> findByAccount(UUID accountKey, int minId, PagingOptions pagination)
    {
        TypedQuery<AccountTipEntity> query = entityManager.createQuery(
            "SELECT r FROM account_tip r " +
                "WHERE r.account.key = :accountKey AND r.id > :minId " +
                "ORDER BY r.id " + (pagination.isAscending()? "ASC" : "DESC"),
                AccountTipEntity.class);

        query.setParameter("accountKey", accountKey);
        query.setParameter("minId", minId);

        int offset = pagination.getOffset();
        if (offset > 0)
            query.setFirstResult(offset);

        query.setMaxResults(pagination.getSize());

        return query.getResultList();
    }

    @Override
    public int countByAccount(UUID accountKey, int minId)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(r.id) FROM account_tip r " +
                "WHERE r.account.key = :accountKey AND r.id > :minId",
            Long.class);
        query.setParameter("accountKey", accountKey);
        query.setParameter("minId", minId);
        return query.getSingleResult().intValue();
    }

    @Override
    public List<AccountTipEntity> findByTip(int tipId)
    {
        return findByTip(tipId, (Interval) null);
    }

    @Override
    public int countByTip(int tipId)
    {
        return countByTip(tipId, (Interval) null);
    }

    @Override
    public List<AccountTipEntity> findByTip(int tipId, Interval interval)
    {
        TypedQuery<AccountTipEntity> query = entityManager.createQuery(
            "SELECT a FROM account_tip a WHERE " +
                "a.tip.id = :rid" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            AccountTipEntity.class);

        query.setParameter("rid", tipId);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }
        return query.getResultList();
    }

    @Override
    public int countByTip(int tipId, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_tip a WHERE " +
                "a.tip.id = :rid" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
             Long.class);

        query.setParameter("rid", tipId);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }
        return query.getSingleResult().intValue();
    }

    @Override
    public List<AccountTipEntity> findByAccountAndTip(UUID accountKey, int tipId)
    {
        return findByAccountAndTip(accountKey, tipId, (Interval) null);
    }

    @Override
    public int countByAccountAndTip(UUID accountKey, int tipId)
    {
        return countByAccountAndTip(accountKey, tipId, (Interval) null);
    }

    @Override
    public List<AccountTipEntity> findByAccountAndTip(UUID accountKey, int tipId, Interval interval)
    {
        TypedQuery<AccountTipEntity> query = entityManager.createQuery(
            "SELECT a FROM account_tip a WHERE " +
                "a.tip.id = :rid AND a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            AccountTipEntity.class);

        query.setParameter("rid", tipId);
        query.setParameter("accountKey", accountKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }
        return query.getResultList();
    }

    @Override
    public int countByAccountAndTip(UUID accountKey, int tipId, Interval interval)
    {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(a.id) FROM account_tip a WHERE " +
                "a.recommendation.id = :rid AND a.account.key = :accountKey" +
                ((interval != null)? " AND a.createdOn >= :start AND a.createdOn < :end" : ""),
            Long.class);

        query.setParameter("rid", tipId);
        query.setParameter("accountKey", accountKey);
        if (interval != null) {
            query.setParameter("start", interval.getStart());
            query.setParameter("end", interval.getEnd());
        }
        return query.getSingleResult().intValue();
    }

    @Override
    public AccountTipEntity findLastForAccount(UUID accountKey)
    {
        TypedQuery<AccountTipEntity> q = entityManager.createQuery(
            "SELECT a FROM account_tip a " +
                "WHERE a.account.key = :accountKey " +
                "ORDER by a.createdOn DESC",
            AccountTipEntity.class);
        q.setParameter("accountKey", accountKey);
        q.setMaxResults(1);

        AccountTipEntity e;
        try {
            e = q.getSingleResult();
        } catch (NoResultException ex) {
            e = null;
        }
        return e;
    }

    @Override
    public AccountTipEntity create(AccountTipEntity e)
    {
        e.setCreatedOn(DateTime.now());
        entityManager.persist(e);
        return e;
    }

    @Override
    public AccountTipEntity createWith(AccountEntity account, int tipId)
    {
        // Ensure we have a persistent AccountEntity instance
        if (!entityManager.contains(account))
            account = entityManager.find(AccountEntity.class, account.getId());

        TipEntity recommendation =
            entityManager.find(TipEntity.class, tipId);
        AccountTipEntity e =
            new AccountTipEntity(account, recommendation);
        return create(e);
    }

    @Override
    public AccountTipEntity createWith(UUID accountKey, int tipId)
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
            return createWith(account, tipId);
    }

    @Override
    public boolean acknowledge(int id, DateTime acknowledged)
    {
        AccountTipEntity r = findOne(id);
        if (r != null)
            return acknowledge(r, acknowledged);
        return false;
    }

    @Override
    public boolean acknowledge(UUID accountKey, int id, DateTime acknowledged)
    {
        AccountTipEntity r = findOne(id);
        // Perform action only if message exists and is owned by account
        if (r != null && r.getAccount().getKey().equals(accountKey))
            return acknowledge(r, acknowledged);
        return false;
    }

    @Override
    public boolean acknowledge(AccountTipEntity r, DateTime acknowledged)
    {
        if (!entityManager.contains(r))
            r = findOne(r.getId());

        if (r != null && r.getAcknowledgedOn() == null) {
            r.setAcknowledgedOn(acknowledged);
            return true;
        }
        return false;
    }

    @Override
    public void delete(int id)
    {
        AccountTipEntity e =
            entityManager.find(AccountTipEntity.class, id);
        if (e != null)
            delete(e);
    }

    @Override
    public void delete(AccountTipEntity e)
    {
        entityManager.remove(e);
    }

    @Override
    public Tip newMessage(int id)
    {
        AccountTipEntity r = findOne(id);
        if (r != null)
            return newMessage(r);
        return null;
    }

    @Override
    public Tip newMessage(AccountTipEntity a)
    {
        TipEntity tipEntity = a.getTip();

        Tip message = new Tip(a.getId());

        message.setIndex(tipEntity.getIndex());
        message.setLocale(tipEntity.getLocale());
        message.setTitle(tipEntity.getTitle());
        message.setDescription(tipEntity.getDescription());
        message.setImage(tipEntity.getImage());
        message.setImageMimeType(tipEntity.getImageMimeType());
        message.setImageLink(tipEntity.getImageLink());
        message.setPrompt(tipEntity.getPrompt());
        message.setExternalLink(tipEntity.getExternalLink());
        message.setSource(tipEntity.getSource());

        if (tipEntity.getModifiedOn() != null)
            message.setModifiedOn(tipEntity.getModifiedOn());
        message.setActive(tipEntity.isActive());

        if (a.getCreatedOn() != null)
            message.setCreatedOn(a.getCreatedOn());
        if (a.getAcknowledgedOn() != null)
            message.setAcknowledgedOn(a.getAcknowledgedOn());

        return message;
    }
}
