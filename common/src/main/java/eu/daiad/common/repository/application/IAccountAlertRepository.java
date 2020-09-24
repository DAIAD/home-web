package eu.daiad.common.repository.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import eu.daiad.common.domain.application.AccountAlertEntity;
import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.AlertResolverExecutionEntity;
import eu.daiad.common.model.PagingOptions;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.message.Alert;
import eu.daiad.common.model.message.EnumAlertType;
import eu.daiad.common.model.message.Alert.ParameterizedTemplate;

public interface IAccountAlertRepository
{
    AccountAlertEntity findOne(int id);

    int countAll();

    
    List<AccountAlertEntity> findByAccount(UUID accountKey);

    List<AccountAlertEntity> findByAccount(UUID accountKey, Interval interval);

    List<AccountAlertEntity> findByAccount(UUID accountKey, int minId);

    List<AccountAlertEntity> findByAccount(UUID accountKey, int minId, PagingOptions pagination);

    int countByAccount(UUID accountKey);

    int countByAccount(UUID accountKey, Interval interval);

    int countByAccount(UUID accountKey, int minId);

    
    List<AccountAlertEntity> findByType(EnumAlertType alertType, UUID utilityKey);

    List<AccountAlertEntity> findByType(EnumAlertType alertType, UUID utilityKey, Interval interval);

    int countByType(EnumAlertType alertType, UUID utilityKey);

    int countByType(EnumAlertType alertType, UUID utilityKey, Interval interval);

    Map<EnumAlertType, Integer> countByType(UUID utilityKey);

    Map<EnumAlertType, Integer> countByType(UUID utilityKey, Interval interval);

    
    List<AccountAlertEntity> findByAccountAndType(UUID accountKey, EnumAlertType alertType);

    List<AccountAlertEntity> findByAccountAndType(UUID accountKey, EnumAlertType alertType, Interval interval);

    int countByAccountAndType(UUID accountKey, EnumAlertType alertType);

    int countByAccountAndType(UUID accountKey, EnumAlertType alertType, Interval interval);

    
    List<AccountAlertEntity> findByExecution(int executionId);
    
    List<AccountAlertEntity> findByExecution(List<Integer> executionIds);
    
    List<AccountAlertEntity> findByAccountAndExecution(UUID accountKey, int executionId);
    
    List<AccountAlertEntity> findByAccountAndExecution(UUID accountKey, List<Integer> executionIds);
    
    int countByExecution(int executionId);
    
    int countByExecution(List<Integer> executionIds);
    
    int countByAccountAndExecution(UUID accountKey, int executionId);
    
    int countByAccountAndExecution(UUID accountKey, List<Integer> executionIds);
    
    
    AccountAlertEntity create(AccountAlertEntity e);

    AccountAlertEntity createWith(
        UUID accountKey,
        ParameterizedTemplate parameterizedTemplate,
        AlertResolverExecutionEntity resolverExecution,
        EnumDeviceType deviceType);

    AccountAlertEntity createWith(
        AccountEntity account,
        ParameterizedTemplate parameterizedTemplate,
        AlertResolverExecutionEntity resolverExecution,
        EnumDeviceType deviceType);

    
    boolean acknowledge(int id, DateTime acknowledged);

    boolean acknowledge(AccountAlertEntity r, DateTime acknowledged);

    boolean acknowledge(UUID accountKey, int id, DateTime acknowledged);

    
    Alert formatMessage(int id, Locale locale);

    Alert formatMessage(AccountAlertEntity r, Locale locale);

    
    void delete(int id);

    void delete(AccountAlertEntity e);
}
