package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.daiad.web.model.admin.Counter;
import eu.daiad.web.model.group.GroupMember;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IUtilityRepository {

    List<UtilityInfo> getUtilities();

    UtilityInfo getUtilityById(int id);

    UtilityInfo getUtilityByKey(UUID key);

    List<GroupMember> getUtilityMembers(UUID key);

    Map<String, Counter> getCounters(int id);

}