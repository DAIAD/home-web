package eu.daiad.web.model.profile;

import eu.daiad.web.model.RestResponse;

public class ProfileModesFilterOptionsResponse extends RestResponse {

	private ProfileModesFilterOptions filterOptions;

	public ProfileModesFilterOptionsResponse(ProfileModesFilterOptions filterOptions) {
		this.filterOptions = filterOptions;
	}

	public ProfileModesFilterOptionsResponse(String code, String description) {
		super(code, description);
	}

	public ProfileModesFilterOptions getFilterOptions() {
		return filterOptions;
	}

}
