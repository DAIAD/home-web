package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.profile.ProfileDeactivateRequest;
import eu.daiad.web.model.profile.ProfileModes;
import eu.daiad.web.model.profile.ProfileModesFilterOptions;
import eu.daiad.web.model.profile.ProfileModesRequest;
import eu.daiad.web.model.profile.ProfileModesSubmitChangesRequest;

public interface IProfileRepository {

	public abstract Profile getProfileByUsername(EnumApplication application) throws ApplicationException;
	
	public abstract List <ProfileModes> getProfileModes(ProfileModesRequest filters) throws ApplicationException;

	public abstract void setProfileConfiguration(EnumApplication application, String value);

	public abstract void notifyProfile(EnumApplication application, UUID version, DateTime updatedOn);

	public abstract ProfileModesFilterOptions getFilterOptions();

	public abstract void setProfileModes(ProfileModesSubmitChangesRequest modeChanges);

	public abstract void deactivateProfile(ProfileDeactivateRequest userDeactId);
}