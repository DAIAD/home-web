package eu.daiad.web.repository.application;

import java.util.List;

import eu.daiad.web.model.group.GroupInfo;

public interface IGroupRepository{
	
	public abstract List <GroupInfo> getGroups();
		
}
