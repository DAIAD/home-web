package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.Interval;

import eu.daiad.web.domain.application.AccountDynamicRecommendationEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.model.message.DynamicRecommendation;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;

public interface IAccountDynamicRecommendationRepository
{
    AccountDynamicRecommendationEntity findOne(int id);
    
    Long countAll();
    
    List<AccountDynamicRecommendationEntity> findByAccount(UUID accountKey);
    
    Long countByAccount(UUID accountKey);
    
    List<AccountDynamicRecommendationEntity> findByAccount(UUID accountKey, Interval interval);
    
    Long countByAccount(UUID accountKey, Interval interval);
    
    List<AccountDynamicRecommendationEntity> findByType(EnumDynamicRecommendationType recommendationType);
    
    Long countByType(EnumDynamicRecommendationType recommendationType);
    
    List<AccountDynamicRecommendationEntity> findByType(
        EnumDynamicRecommendationType recommendationType, Interval interval);
    
    Long countByType(
        EnumDynamicRecommendationType recommendationType, Interval interval);
    
    List<AccountDynamicRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType);
    
    Long countByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType);
    
    List<AccountDynamicRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType, Interval interval);
    
    Long countByAccountAndType(
        UUID accountKey, EnumDynamicRecommendationType recommendationType, Interval interval);
    
    AccountDynamicRecommendationEntity create(AccountDynamicRecommendationEntity e);
    
    AccountDynamicRecommendationEntity createWith(
        UUID accountKey,  DynamicRecommendation.Parameters parameters);
    
    AccountDynamicRecommendationEntity createWith(
        AccountEntity account, DynamicRecommendation.Parameters parameters);
    
    void delete(int id);
    
    void delete(AccountDynamicRecommendationEntity e);
}
