package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.Interval;
import org.joda.time.Period;

import eu.daiad.web.domain.application.AccountAlertEntity;
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
}
