package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.group.CreateGroupSetRequest;
import eu.daiad.web.model.group.GroupInfo;
import eu.daiad.web.model.group.GroupMemberInfo;
import eu.daiad.web.model.admin.AccountActivity;

public interface IUserGroupRepository{
	
	public abstract List <GroupInfo> getGroups();
	
	public abstract List <GroupMemberInfo> getGroupCurrentMembers(UUID group_id);
    
    public List<AccountActivity> getGroupAccounts(UUID group_id);

	public abstract List<GroupMemberInfo> getGroupPossibleMembers(UUID group_id);

	public abstract void createGroupSet(CreateGroupSetRequest groupSetInfo);

	public abstract GroupInfo getSingleGroupByKey(UUID group_id);

	public abstract void deleteGroup(UUID group_id);
		
	public abstract List<GroupInfo> getGroupsByMember(UUID user_id);
}
