package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.TipEntity;
import eu.daiad.web.model.message.Tip;

@Repository
@Transactional("applicationTransactionManager")
public class TipRepository
    implements ITipRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public TipEntity findOne(int id)
    {
        return entityManager.find(TipEntity.class, id);
    }

    @Override
    public TipEntity findOne(int index, Locale locale)
    {
        TypedQuery<TipEntity> query = entityManager.createQuery(
            "SELECT r FROM tip r WHERE r.index = :index AND r.locale = :locale",
            TipEntity.class);
        query.setParameter("index", index);
        query.setParameter("locale", locale.getLanguage());

        TipEntity e;
        try {
            e = query.getSingleResult();
        } catch (NoResultException x) {
            // Note: Maybe we should retry with default locale?
            e = null;
        }
        return e;
    }

    @Override
    public List<TipEntity> findByLocale(Locale locale)
    {
        TypedQuery<TipEntity> query = entityManager.createQuery(
            "SELECT r FROM tip r WHERE r.locale = :locale", TipEntity.class);
        query.setParameter("locale", locale.getLanguage());
        return query.getResultList();
    }

    @Override
    public TipEntity randomOne(Locale locale)
    {
        List<TipEntity> entities = random(locale, 1);
        return entities.isEmpty()? null : entities.get(0);
    }

    @Override
    public List<TipEntity> random(Locale locale, int size)
    {
        if (size < 1)
            throw new IllegalArgumentException("size must be a positive integer");

        TypedQuery<Integer> query = entityManager.createQuery(
            "SELECT r.id FROM tip r WHERE r.locale = :locale", Integer.class);
        query.setParameter("locale", locale.getLanguage());

        List<Integer> rids = query.getResultList();
        Collections.shuffle(rids);

        size = Math.min(size, rids.size());
        List<TipEntity> results = new ArrayList<>(size);
        for (Integer rid: rids.subList(0, size)) {
            results.add(findOne(rid));
        }
        return results;
    }

    @Override
    public Tip newMessage(int id)
    {
        TipEntity r = findOne(id);
        if (r != null)
            return newMessage(r);
        return null;
    }

    @Override
    public Tip newMessage(TipEntity r)
    {
        Tip message = new Tip(r.getId());

        message.setIndex(r.getIndex());
        message.setLocale(r.getLocale());
        message.setTitle(r.getTitle());
        message.setDescription(r.getDescription());
        //message.setImageEncoded(r.getImage());
        message.setImageMimeType(r.getImageMimeType());
        message.setImageLink(r.getImageLink());
        message.setPrompt(r.getPrompt());
        message.setExternalLink(r.getExternalLink());
        message.setSource(r.getSource());

        message.setCreatedOn(r.getCreatedOn());
        if (r.getModifiedOn() != null)
            message.setModifiedOn(r.getModifiedOn());

        message.setActive(r.isActive());

        return message;
    }

    private int maxIndex()
    {
        TypedQuery<Long> q =
            entityManager.createQuery("SELECT MAX(r.index) FROM tip r", Long.class);
        return q.getSingleResult().intValue();
    }

    @Override
    public TipEntity create(TipEntity r)
    {
        entityManager.persist(r);
        return r;
    }

    @Override
    public TipEntity createFrom(Tip tip)
    {
        DateTime now = DateTime.now();

        TipEntity r = new TipEntity();

        int index = tip.getIndex();
        if (index > 0) {
            // Use this as index
            r.setIndex(index);
        } else {
            // Obtain the next available index
            r.setIndex(maxIndex() + 1);
        }
        r.setLocale(tip.getLocale());

        r.setTitle(tip.getTitle());
        r.setDescription(r.getDescription());
        //r.setImageEncoded(tip.getImage());
        r.setImageMimeType(tip.getImageMimeType());
        r.setImageLink(tip.getImageLink());
        r.setPrompt(tip.getPrompt());
        r.setExternalLink(tip.getExternalLink());
        r.setSource(tip.getSource());

        r.setCreatedOn(now);
        r.setActive(tip.isActive());

        return create(r);
    }

    @Override
    public TipEntity updateFrom(TipEntity r, Tip tip)
    {
        DateTime now = DateTime.now();

        TipEntity r1 = new TipEntity();
        r1.setId(r.getId());

        // Set update-able fields and merge
        // Note: The pair (index, locale) is not meant to be updated

        r1.setTitle(tip.getTitle());
        r1.setDescription(tip.getDescription());
        //r1.setImageEncoded(tip.getImage());
        r1.setImageMimeType(tip.getImageMimeType());
        r1.setImageLink(tip.getImageLink());
        r1.setPrompt(tip.getPrompt());
        r1.setExternalLink(tip.getExternalLink());
        r1.setSource(tip.getSource());
        r1.setActive(tip.isActive());
        r1.setModifiedOn(now);

        return entityManager.merge(r1);
    }

    @Override
    public TipEntity saveFrom(Tip tip)
    {
        int id = tip.getId();
        if (id > 0) {
            // Todo
        }


        return null;
    }

    @Override
    public void delete(int id)
    {
        TipEntity r = findOne(id);
        if (r != null)
            delete(r);
    }

    @Override
    public void delete(TipEntity r)
    {
        entityManager.remove(r);
    }

    @Override
    public void setActive(int id, boolean active)
    {
        TipEntity r = findOne(id);
        if (r != null)
            setActive(r, active);
    }

    @Override
    public void setActive(TipEntity r, boolean active)
    {
        r.setActive(active);
    }
}
