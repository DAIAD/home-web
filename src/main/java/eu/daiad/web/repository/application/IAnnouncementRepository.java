package eu.daiad.web.repository.application;

import java.util.Map;
import java.util.Set;

import eu.daiad.web.domain.application.AnnouncementEntity;
import eu.daiad.web.model.message.Message;

public interface IAnnouncementRepository
{
    AnnouncementEntity findOne(int id);

    AnnouncementEntity create(AnnouncementEntity e);

    AnnouncementEntity createWith(Set<String> channelNames, Map<String, Message> translationByLanguage);

    AnnouncementEntity createWith(Set<String> channelNames, Map<String, Message> translationByLanguage, int priority);

    void delete(int id);

    void delete(AnnouncementEntity e);
}
