package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.Interval;

import eu.daiad.web.domain.application.AccountRecommendationEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.model.message.Recommendation;
import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.EnumRecommendationType;

public interface IAccountRecommendationRepository
{
    AccountRecommendationEntity findOne(int id);
    
    Long countAll();
    
    List<AccountRecommendationEntity> findByAccount(UUID accountKey);
    
    Long countByAccount(UUID accountKey);
    
    List<AccountRecommendationEntity> findByAccount(UUID accountKey, Interval interval);
    
    Long countByAccount(UUID accountKey, Interval interval);
    
    List<AccountRecommendationEntity> findByType(EnumRecommendationType recommendationType);
    
    Long countByType(EnumRecommendationType recommendationType);
    
    List<AccountRecommendationEntity> findByType(
        EnumRecommendationType recommendationType, Interval interval);
    
    Long countByType(
        EnumRecommendationType recommendationType, Interval interval);
    
    List<AccountRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType);
    
    Long countByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType);
    
    List<AccountRecommendationEntity> findByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType, Interval interval);
    
    Long countByAccountAndType(
        UUID accountKey, EnumRecommendationType recommendationType, Interval interval);
    
    AccountRecommendationEntity create(AccountRecommendationEntity e);
    
    AccountRecommendationEntity createWith(
        UUID accountKey,  Recommendation.ParameterizedTemplate parameterizedTemplate);
    
    AccountRecommendationEntity createWith(
        AccountEntity account, Recommendation.ParameterizedTemplate parameterizedTemplate);
    
    void delete(int id);
    
    void delete(AccountRecommendationEntity e);
}
