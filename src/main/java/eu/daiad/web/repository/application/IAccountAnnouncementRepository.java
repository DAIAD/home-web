package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.Interval;

import eu.daiad.web.domain.application.AccountAnnouncementEntity;
import eu.daiad.web.domain.application.AccountStaticRecommendationEntity;

public interface IAccountAnnouncementRepository
{
    AccountAnnouncementEntity findOne(int id);
    
    List<AccountAnnouncementEntity> findByAccount(UUID accountKey);
    
    List<AccountAnnouncementEntity> findByAccount(UUID accountKey, Interval interval);
    
    List<AccountAnnouncementEntity> findByType(int announcementType);
    
    List<AccountAnnouncementEntity> findByType(int announcementType, Interval interval);
    
    List<AccountAnnouncementEntity> findByAccountAndType(UUID accountKey, int announcementType);
    
    List<AccountAnnouncementEntity> findByAccountAndType(UUID accountKey, int announcementType, Interval interval);
}
