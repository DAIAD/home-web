package eu.daiad.web.repository.application;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import eu.daiad.web.domain.application.AlertResolverExecutionEntity;
import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.UtilityEntity;

public interface IAlertResolverExecutionRepository
{
    AlertResolverExecutionEntity findOne(int rid);
    
    List<AlertResolverExecutionEntity> findByName(String resolverName);
    
    List<AlertResolverExecutionEntity> findByName(String resolverName, Interval interval);
    
    List<Integer> findIdByName(String resolverName);
    
    List<Integer> findIdByName(String resolverName, Interval interval);
        
    AlertResolverExecutionEntity create(AlertResolverExecutionEntity r);
    
    AlertResolverExecutionEntity createWith(DateTime refDate, String resolverName, UtilityEntity target, DateTime started);
    
    AlertResolverExecutionEntity createWith(DateTime refDate, String resolverName, GroupEntity target, DateTime started);
    
    AlertResolverExecutionEntity updateFinished(AlertResolverExecutionEntity r, DateTime finished);
    
    AlertResolverExecutionEntity updateFinished(int rid, DateTime finished);
    
    void delete(int rid);
    
    void delete(AlertResolverExecutionEntity r);
}
