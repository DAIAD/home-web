package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.utility.UtilityInfo;

public interface IUtilityRepository {

	public abstract List<UtilityInfo> getUtilities();

	public abstract UtilityInfo getUtilityById(int id);
	
	public abstract UtilityInfo getUtilityByKey(UUID key);

}