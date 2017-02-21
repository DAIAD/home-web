package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.model.admin.Counter;
import eu.daiad.web.model.group.GroupMember;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IUtilityRepository 
{
    UtilityEntity findOne(int id);
    
    UtilityEntity findOne(UUID key);
    
    List<UtilityInfo> getUtilities();

    UtilityInfo getUtilityById(int id);

    UtilityInfo getUtilityByKey(UUID key);

    List<GroupMember> getUtilityMembers(UUID key);

    List<UUID> getMembers(UUID utilityKey);
    
    List<UUID> getMembers(int utilityId);
    
    Map<String, Counter> getCounters(int id);

}