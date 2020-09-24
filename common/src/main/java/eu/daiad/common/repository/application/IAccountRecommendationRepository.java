package eu.daiad.common.repository.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.AccountRecommendationEntity;
import eu.daiad.common.domain.application.RecommendationResolverExecutionEntity;
import eu.daiad.common.model.PagingOptions;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.message.EnumMessageLevel;
import eu.daiad.common.model.message.EnumRecommendationType;
import eu.daiad.common.model.message.Recommendation;
import eu.daiad.common.model.message.Recommendation.ParameterizedTemplate;

public interface IAccountRecommendationRepository
{
    AccountRecommendationEntity findOne(int id);

    int countAll();


    List<AccountRecommendationEntity> findByAccount(UUID accountKey);

    List<AccountRecommendationEntity> findByAccount(UUID accountKey, Interval interval);

    List<AccountRecommendationEntity> findByAccount(UUID accountKey, int minId, PagingOptions pagination, EnumMessageLevel significant);

    int countByAccount(UUID accountKey);

    int countByAccount(UUID accountKey, Interval interval);

    int countByAccount(UUID accountKey, int minId, EnumMessageLevel significant);


    List<AccountRecommendationEntity> findByType(EnumRecommendationType recommendationType, UUID utilityKey);

    List<AccountRecommendationEntity> findByType(EnumRecommendationType recommendationType, UUID utilityKey, Interval interval);

    int countByType(EnumRecommendationType recommendationType, UUID utilityKey);

    int countByType(EnumRecommendationType recommendationType, UUID utilityKey, Interval interval);

    Map<EnumRecommendationType, Integer> countByType(UUID utilityKey);

    Map<EnumRecommendationType, Integer> countByType(UUID utilityKey, Interval interval);


    List<AccountRecommendationEntity> findByAccountAndType(UUID accountKey, EnumRecommendationType recommendationType);

    List<AccountRecommendationEntity> findByAccountAndType(UUID accountKey, EnumRecommendationType recommendationType, Interval interval);

    int countByAccountAndType(UUID accountKey, EnumRecommendationType recommendationType);

    int countByAccountAndType(UUID accountKey, EnumRecommendationType recommendationType, Interval interval);


    List<AccountRecommendationEntity> findByExecution(int executionId);

    List<AccountRecommendationEntity> findByExecution(List<Integer> executionIds);

    List<AccountRecommendationEntity> findByAccountAndExecution(UUID accountKey, int executionId);

    List<AccountRecommendationEntity> findByAccountAndExecution(UUID accountKey, List<Integer> executionIds);

    int countByExecution(int executionId);

    int countByExecution(List<Integer> executionIds);

    int countByAccountAndExecution(UUID accountKey, int executionId);

    int countByAccountAndExecution(UUID accountKey, List<Integer> executionIds);


    AccountRecommendationEntity create(AccountRecommendationEntity e);

    AccountRecommendationEntity createWith(
        UUID accountKey,
        ParameterizedTemplate parameterizedTemplate,
        RecommendationResolverExecutionEntity resolverExecution,
        EnumDeviceType deviceType,
        EnumMessageLevel level);

    AccountRecommendationEntity createWith(
        AccountEntity account,
        ParameterizedTemplate parameterizedTemplate,
        RecommendationResolverExecutionEntity resolverExecution,
        EnumDeviceType deviceType,
        EnumMessageLevel level);


    boolean acknowledge(int id, DateTime acknowledged);

    boolean acknowledge(AccountRecommendationEntity r, DateTime acknowledged);

    boolean acknowledge(UUID accountKey, int id, DateTime acknowledged);


    Recommendation formatMessage(int id, Locale locale);

    Recommendation formatMessage(AccountRecommendationEntity r, Locale locale);


    void delete(int id);

    void delete(AccountRecommendationEntity e);
}
