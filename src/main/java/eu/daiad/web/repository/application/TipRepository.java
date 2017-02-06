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
import org.springframework.util.Assert;

import eu.daiad.web.domain.application.TipCategoryEntity;
import eu.daiad.web.domain.application.TipEntity;
import eu.daiad.web.model.message.Tip;

@Repository
@Transactional("applicationTransactionManager")
public class TipRepository
    implements ITipRepository
{
    public static final String DEFAULT_CATEGORY_NAME = "general-tips";

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
        message.setImage(r.getImage());
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
        TypedQuery<Integer> q =
            entityManager.createQuery("SELECT MAX(r.index) FROM tip r", Integer.class);
        return q.getSingleResult().intValue();
    }

    private TipCategoryEntity findCategoryByName(String name)
    {
        TypedQuery<TipCategoryEntity> q = entityManager.createQuery(
            "SELECT c FROM tip_category c WHERE c.name = :name",
            TipCategoryEntity.class);
        q.setParameter("name", name);

        TipCategoryEntity category = null;
        try {
            category = q.getSingleResult();
        } catch (NoResultException x) {
            category = null;
        }

        return category;
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
            // Note: This is not a completely safe way to obtain a unique index!
            r.setIndex(maxIndex() + 1);
        }
        r.setLocale(tip.getLocale());

        TipCategoryEntity category = findCategoryByName(
            (tip.getCategoryName() != null)? tip.getCategoryName() : DEFAULT_CATEGORY_NAME);
        r.setCategory(category);

        r.setTitle(tip.getTitle());
        r.setDescription(tip.getDescription());
        r.setImage(tip.getImage());
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

        int rid = r.getId();
        Assert.state(rid > 0);

        if (!entityManager.contains(r))
            r = findOne(rid);

        // Set update-able fields and merge
        // Note: The pair (index, locale) is not meant to be updated

        if (tip.getCategoryName() != null) {
            r.setCategory(findCategoryByName(tip.getCategoryName()));
        }
        r.setTitle(tip.getTitle());
        r.setDescription(tip.getDescription());
        r.setImage(tip.getImage());
        r.setImageMimeType(tip.getImageMimeType());
        r.setImageLink(tip.getImageLink());
        r.setPrompt(tip.getPrompt());
        r.setExternalLink(tip.getExternalLink());
        r.setSource(tip.getSource());
        r.setActive(tip.isActive());
        r.setModifiedOn(now);

        return r;
    }

    @Override
    public TipEntity saveFrom(Tip tip)
    {
        // Decide if this is an UPDATE/INSERT

        int id = tip.getId();
        if (id > 0) {
            // Find tip entity by id and update
            TipEntity r = findOne(id);
            return updateFrom(r, tip);
        }

        int index = tip.getIndex();
        String lang = tip.getLocale();
        if (index > 0 && lang != null && !lang.isEmpty()) {
            // Find tip by (index, locale) and update
            TipEntity r = findOne(index, Locale.forLanguageTag(lang));
            return (r != null)? updateFrom(r, tip): null;
        }

        // This is not an update, so create a new entity

        return createFrom(tip);
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
            r.setActive(active);
    }

    @Override
    public void setActive(TipEntity r, boolean active)
    {
        if (!entityManager.contains(r))
            r = findOne(r.getId());
        if (r != null)
            r.setActive(active);
    }
}
