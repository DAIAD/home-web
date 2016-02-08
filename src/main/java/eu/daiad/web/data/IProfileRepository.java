package eu.daiad.web.data;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.Profile;

public interface IProfileRepository {

	public abstract Profile getProfileByUsername(String username) throws ApplicationException;

}