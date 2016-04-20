package eu.daiad.web.repository.application;

import java.util.List;

import eu.daiad.web.model.utility.UtilityInfo;

public interface IUtilityRepository{
	
	public abstract List <UtilityInfo> getUtilities();
		
}