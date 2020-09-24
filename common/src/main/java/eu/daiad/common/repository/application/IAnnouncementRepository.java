package eu.daiad.common.repository.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.daiad.common.domain.application.AnnouncementEntity;
import eu.daiad.common.model.message.Announcement;
import eu.daiad.common.model.message.Message;

public interface IAnnouncementRepository
{
    AnnouncementEntity findOne(int id);

    List<AnnouncementEntity> list();

    AnnouncementEntity create(AnnouncementEntity e);

    AnnouncementEntity createWith(List<String> channelNames, Map<String, Message> translationByLanguage);

    AnnouncementEntity createWith(List<String> channelNames, Map<String, Message> translationByLanguage, int priority);

    void delete(int id);

    void delete(AnnouncementEntity e);

    Announcement newMessage(int id, Locale locale);

    Announcement newMessage(AnnouncementEntity a, Locale locale);
}
