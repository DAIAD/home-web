package eu.daiad.common.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.profile.Profile;
import eu.daiad.common.model.profile.ProfileDeactivateRequest;
import eu.daiad.common.model.profile.ProfileHistoryEntry;
import eu.daiad.common.model.profile.ProfileModes;
import eu.daiad.common.model.profile.ProfileModesFilterOptions;
import eu.daiad.common.model.profile.ProfileModesRequest;
import eu.daiad.common.model.profile.ProfileModesSubmitChangesRequest;
import eu.daiad.common.model.profile.UpdateHouseholdRequest;
import eu.daiad.common.model.profile.UpdateProfileRequest;

public interface IProfileRepository {

    Profile getProfileByUserKey(UUID userKey, EnumApplication application) throws ApplicationException;

    List<ProfileModes> getProfileModes(ProfileModesRequest filters) throws ApplicationException;

    void saveProfile(UpdateProfileRequest updates);

    void notifyProfile(EnumApplication application, UUID version, DateTime updatedOn);

    ProfileModesFilterOptions getFilterOptions();

    void setProfileModes(ProfileModesSubmitChangesRequest modeChanges);

    void deactivateProfile(ProfileDeactivateRequest request);

    void saveHousehold(UpdateHouseholdRequest updates);

    List<ProfileHistoryEntry> getProfileHistoryByUserKey(UUID userKey);

    void updateMobileVersion(UUID userKey, String version);

}
