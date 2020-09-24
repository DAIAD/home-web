package eu.daiad.common.repository.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.daiad.common.domain.application.UtilityEntity;
import eu.daiad.common.model.admin.Counter;
import eu.daiad.common.model.group.GroupMember;
import eu.daiad.common.model.utility.UtilityInfo;

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