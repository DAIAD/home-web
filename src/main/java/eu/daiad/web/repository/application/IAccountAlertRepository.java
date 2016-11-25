package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.Interval;

import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.model.message.EnumAlertType;

public interface IAccountAlertRepository
{
    AccountAlertEntity findOne(int id);
    
    List<AccountAlertEntity> findByAccount(UUID accountKey);
    
    List<AccountAlertEntity> findByAccount(UUID accountKey, Interval interval);
    
    List<AccountAlertEntity> findByType(EnumAlertType alertType);
    
    List<AccountAlertEntity> findByType(EnumAlertType alertType, Interval interval);
    
    List<AccountAlertEntity> findByAccountAndType(UUID accountKey, EnumAlertType alertType);
    
    List<AccountAlertEntity> findByAccountAndType(UUID accountKey, EnumAlertType alertType, Interval interval);
    
    AccountAlertEntity create(AccountAlertEntity e);
    
    AccountAlertEntity createWith(AccountEntity account, EnumAlertType alertType, Map<String, Object> p);
    
    void delete(int id);
    
    void delete(AccountAlertEntity e);
}
