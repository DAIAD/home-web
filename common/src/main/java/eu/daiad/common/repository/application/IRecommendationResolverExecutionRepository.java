package eu.daiad.common.repository.application;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import eu.daiad.common.domain.application.GroupEntity;
import eu.daiad.common.domain.application.RecommendationResolverExecutionEntity;
import eu.daiad.common.domain.application.UtilityEntity;

public interface IRecommendationResolverExecutionRepository
{
    RecommendationResolverExecutionEntity findOne(int rid);
    
    List<RecommendationResolverExecutionEntity> findByName(String resolverName);
    
    List<RecommendationResolverExecutionEntity> findByName(String resolverName, Interval interval);
    
    List<Integer> findIdByName(String resolverName);
    
    List<Integer> findIdByName(String resolverName, Interval interval);
    
    RecommendationResolverExecutionEntity create(RecommendationResolverExecutionEntity r);
    
    RecommendationResolverExecutionEntity createWith(DateTime refDate, String resolverName, UtilityEntity target, DateTime started);
    
    RecommendationResolverExecutionEntity createWith(DateTime refDate, String resolverName, GroupEntity target, DateTime started);
    
    RecommendationResolverExecutionEntity updateFinished(RecommendationResolverExecutionEntity r, DateTime finished);
    
    RecommendationResolverExecutionEntity updateFinished(int rid, DateTime finished);
    
    void delete(int rid);
    
    void delete(RecommendationResolverExecutionEntity r);
}
