package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.Interval;

import eu.daiad.web.domain.application.AccountDynamicRecommendationEntity;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;

public interface IAccountDynamicRecommendationRepository
{
    AccountDynamicRecommendationEntity findOne(int id);
    
    List<AccountDynamicRecommendationEntity> findByAccount(UUID accountKey);
    
    List<AccountDynamicRecommendationEntity> findByAccount(UUID accountKey, Interval interval);
    
    List<AccountDynamicRecommendationEntity> findByType(
        EnumDynamicRecommendationType recommendationType);
    
    List<AccountDynamicRecommendationEntity> findByType(
        EnumDynamicRecommendationType recommendationType, Interval interval);
    
    List<AccountDynamicRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType);
    
    List<AccountDynamicRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType, Interval interval);
}
