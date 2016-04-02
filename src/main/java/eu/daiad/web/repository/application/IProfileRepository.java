package eu.daiad.web.repository.application;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.Profile;

public interface IProfileRepository {

	public abstract Profile getProfileByUsername(EnumApplication application) throws ApplicationException;

	public abstract void setProfileConfiguration(EnumApplication application, String value);

	public abstract void notifyProfile(EnumApplication application, UUID version, DateTime updatedOn);
}