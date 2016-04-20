package eu.daiad.web.model.utility;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class UtilityInfoResponse extends RestResponse{
	
	private List <UtilityInfo> utilitiesInfo;

	public UtilityInfoResponse(List <UtilityInfo> utilitiesInfo) {
		this.utilitiesInfo = utilitiesInfo;
	}

	public UtilityInfoResponse(String code, String description) {
		super(code, description);
	}

	public List <UtilityInfo> getUtilitiesInfo() {
		return utilitiesInfo;
	}
}