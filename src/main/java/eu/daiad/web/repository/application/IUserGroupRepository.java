package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.group.CreateGroupSetRequest;
import eu.daiad.web.model.group.GroupInfo;
import eu.daiad.web.model.group.GroupMemberInfo;

public interface IUserGroupRepository{

	List <GroupInfo> getGroups();

	List <GroupMemberInfo> getGroupCurrentMembers(UUID key);

    List<AccountActivity> getGroupAccounts(UUID key);

	List<GroupMemberInfo> getGroupPossibleMembers(UUID key);

	void createGroupSet(CreateGroupSetRequest groupSetInfo);

	GroupInfo getSingleGroupByKey(UUID key);

	void deleteGroup(UUID key);

	List<GroupInfo> getGroupsByMember(UUID key);
}
