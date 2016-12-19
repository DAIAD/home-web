package eu.daiad.web.repository.application;

import java.util.Locale;

import eu.daiad.web.domain.application.AnnouncementEntity;
import eu.daiad.web.domain.application.AnnouncementTranslationEntity;

public interface IAnnouncementRepository
{
    AnnouncementEntity findOne(int announcementId);
}
