package eu.daiad.common.model.profile;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class ProfileModesFilterOptionsResponse extends RestResponse {

	private ProfileModesFilterOptions filterOptions;

	public ProfileModesFilterOptionsResponse(ProfileModesFilterOptions filterOptions) {
		this.filterOptions = filterOptions;
	}

	public ProfileModesFilterOptionsResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public ProfileModesFilterOptions getFilterOptions() {
		return filterOptions;
	}

}
