package eu.daiad.common.repository.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import eu.daiad.common.domain.application.AccountAnnouncementEntity;
import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.AnnouncementEntity;
import eu.daiad.common.model.PagingOptions;
import eu.daiad.common.model.message.Announcement;

public interface IAccountAnnouncementRepository
{
    AccountAnnouncementEntity findOne(int id);

    AccountAnnouncementEntity findOne(UUID accountKey, int announcementId);

    int countAll();

    List<AccountAnnouncementEntity> findByAccount(UUID accountKey);

    List<AccountAnnouncementEntity> findByAccount(UUID accountKey, Interval interval);

    List<AccountAnnouncementEntity> findByAccount(UUID accountKey, int minId);

    List<AccountAnnouncementEntity> findByAccount(UUID accountKey, int minId, PagingOptions pagination);

    int countByAccount(UUID accountKey, Interval interval);

    int countByAccount(UUID accountKey);

    int countByAccount(UUID accountKey, int minId);

    List<AccountAnnouncementEntity> findByAnnouncement(int announcementId);

    List<AccountAnnouncementEntity> findByAnnouncement(int announcementId, Interval interval);

    int countByAnnouncement(int announcementId);

    int countByAnnouncement(int announcementId, Interval interval);

    AccountAnnouncementEntity create(AccountAnnouncementEntity e);

    AccountAnnouncementEntity createWith(UUID accountKey, int announcementId);

    AccountAnnouncementEntity createWith(AccountEntity account, int announcementId);

    AccountAnnouncementEntity createWith(AccountEntity account, AnnouncementEntity announcement);

    boolean acknowledge(int id, DateTime acknowledged);

    boolean acknowledge(AccountAnnouncementEntity r, DateTime acknowledged);

    boolean acknowledge(UUID accountKey, int id, DateTime acknowledged);

    Announcement newMessage(int id, Locale locale);

    Announcement newMessage(AccountAnnouncementEntity r, Locale locale);

    void delete(int id);

    void delete(AccountAnnouncementEntity e);
}
