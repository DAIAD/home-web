package eu.daiad.web.data;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.Profile;

public interface IProfileRepository {

	public abstract Profile getProfileByUsername(EnumApplication application, String username) throws ApplicationException;

	public abstract void setProfileConfiguration(EnumApplication application, String value);

}