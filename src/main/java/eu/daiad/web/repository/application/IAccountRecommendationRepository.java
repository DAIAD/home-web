package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountRecommendationEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.message.EnumRecommendationType;
import eu.daiad.web.model.message.Recommendation;

public interface IAccountRecommendationRepository
{
    AccountRecommendationEntity findOne(int id);

    int countAll();

    List<AccountRecommendationEntity> findByAccount(UUID accountKey);

    List<AccountRecommendationEntity> findByAccount(UUID accountKey, Interval interval);

    List<AccountRecommendationEntity> findByAccount(UUID accountKey, int minId);

    List<AccountRecommendationEntity> findByAccount(UUID accountKey, int minId, PagingOptions pagination);

    int countByAccount(UUID accountKey);

    int countByAccount(UUID accountKey, Interval interval);

    int countByAccount(UUID accountKey, int minId);

    List<AccountRecommendationEntity> findByType(EnumRecommendationType recommendationType, UUID utilityKey);

    List<AccountRecommendationEntity> findByType(EnumRecommendationType recommendationType, UUID utilityKey, Interval interval);

    int countByType(EnumRecommendationType recommendationType, UUID utilityKey);

    int countByType(EnumRecommendationType recommendationType, UUID utilityKey, Interval interval);

    Map<EnumRecommendationType, Long> countByType(UUID utilityKey);

    Map<EnumRecommendationType, Long> countByType(UUID utilityKey, Interval interval);

    List<AccountRecommendationEntity> findByAccountAndType(UUID accountKey, EnumRecommendationType recommendationType);

    List<AccountRecommendationEntity> findByAccountAndType(UUID accountKey, EnumRecommendationType recommendationType, Interval interval);

    int countByAccountAndType(UUID accountKey, EnumRecommendationType recommendationType);

    int countByAccountAndType(UUID accountKey, EnumRecommendationType recommendationType, Interval interval);

    AccountRecommendationEntity create(AccountRecommendationEntity e);

    AccountRecommendationEntity createWith(UUID accountKey, Recommendation.ParameterizedTemplate parameterizedTemplate);

    AccountRecommendationEntity createWith(AccountEntity account, Recommendation.ParameterizedTemplate parameterizedTemplate);

    boolean acknowledge(int id, DateTime acknowledged);

    boolean acknowledge(AccountRecommendationEntity r, DateTime acknowledged);

    boolean acknowledge(UUID accountKey, int id, DateTime acknowledged);

    Recommendation formatMessage(int id, Locale locale);

    Recommendation formatMessage(AccountRecommendationEntity r, Locale locale);

    void delete(int id);

    void delete(AccountRecommendationEntity e);
}
