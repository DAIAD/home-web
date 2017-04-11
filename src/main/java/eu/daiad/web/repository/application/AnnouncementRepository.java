package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AnnouncementEntity;
import eu.daiad.web.domain.application.AnnouncementTranslationEntity;
import eu.daiad.web.domain.application.ChannelEntity;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.Message;

@Repository
@Transactional("applicationTransactionManager")
public class AnnouncementRepository
    implements IAnnouncementRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    public static final int DEFAULT_PRIORITY = 5;

    @Override
    public AnnouncementEntity findOne(int id)
    {
        return entityManager.find(AnnouncementEntity.class, id);
    }

    @Override
    public List<AnnouncementEntity> list()
    {
        TypedQuery<AnnouncementEntity> query =
            entityManager.createQuery("FROM announcement a", AnnouncementEntity.class);
        return query.getResultList();
    }

    @Override
    public AnnouncementEntity create(AnnouncementEntity a)
    {
        entityManager.persist(a);
        return a;
    }

    @Override
    public AnnouncementEntity createWith(
        List<String> channelNames, Map<String, Message> translationByLanguage)
    {
        return createWith(channelNames, translationByLanguage, DEFAULT_PRIORITY);
    }

    @Override
    public AnnouncementEntity createWith(
        List<String> channelNames, Map<String, Message> translationByLanguage, int priority)
    {
        AnnouncementEntity a = new AnnouncementEntity(priority);

        TypedQuery<ChannelEntity> channelQuery = entityManager.createQuery(
            "SELECT c FROM channel c WHERE c.name IN (:names)", ChannelEntity.class);
        channelQuery.setParameter("names", channelNames);

        for (ChannelEntity c: channelQuery.getResultList())
            a.addChannel(c);

        for (String lang: translationByLanguage.keySet()) {
            Locale locale = Locale.forLanguageTag(lang);
            Message message = translationByLanguage.get(lang);
            a.addTranslation(locale, message.getTitle(), message.getBody());
        }

        return create(a);
    }

    @Override
    public void delete(int id)
    {
        AnnouncementEntity e = findOne(id);
        if (e != null)
            delete(e);
    }

    @Override
    public void delete(AnnouncementEntity e)
    {
        if (!entityManager.contains(e))
            e = findOne(e.getId());
        if (e != null)
            entityManager.remove(e);
    }

    @Override
    public Announcement newMessage(int id, Locale locale)
    {
        AnnouncementEntity a = findOne(id);
        if (a != null)
            return newMessage(a, locale);
        return null;
    }

    @Override
    public Announcement newMessage(AnnouncementEntity a, Locale locale)
    {
        AnnouncementTranslationEntity translation = null;
        translation = a.getTranslation(locale);
        if (translation == null) {
            locale = Locale.getDefault();
            translation = a.getTranslation(locale);
        }
        if (translation == null)
            return null;

        Announcement message = new Announcement(a.getId());
        message.setPriority(a.getPriority());
        message.setLocale(locale.getLanguage());
        message.setTitle(translation.getTitle());
        message.setContent(translation.getContent());
        return message;
    }
}
