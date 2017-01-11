package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.profile.ProfileDeactivateRequest;
import eu.daiad.web.model.profile.ProfileHistoryEntry;
import eu.daiad.web.model.profile.ProfileModes;
import eu.daiad.web.model.profile.ProfileModesFilterOptions;
import eu.daiad.web.model.profile.ProfileModesRequest;
import eu.daiad.web.model.profile.ProfileModesSubmitChangesRequest;
import eu.daiad.web.model.profile.UpdateHouseholdRequest;
import eu.daiad.web.model.profile.UpdateLayoutRequest;
import eu.daiad.web.model.profile.UpdateProfileRequest;

public interface IProfileRepository {

    Profile getProfileByUsername(EnumApplication application) throws ApplicationException;

    List<ProfileModes> getProfileModes(ProfileModesRequest filters) throws ApplicationException;

    void saveProfile(UpdateProfileRequest updates);

    void notifyProfile(EnumApplication application, UUID version, DateTime updatedOn);

    ProfileModesFilterOptions getFilterOptions();

    void setProfileModes(ProfileModesSubmitChangesRequest modeChanges);

    void deactivateProfile(ProfileDeactivateRequest userDeactId);

    void saveHousehold(UpdateHouseholdRequest updates);

    List<ProfileHistoryEntry> getProfileHistoryByUserKey(UUID userKey);

    void updateMobileVersion(UUID userKey, String version);
    
    void saveProfileLayout(UpdateLayoutRequest updates);

}
