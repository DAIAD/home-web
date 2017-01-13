package eu.daiad.web.model.profile;

import eu.daiad.web.model.RestResponse;
import java.util.List;

public class ProfileLayoutResponse extends RestResponse {

	private List<LayoutComponent> layouts;

	public ProfileLayoutResponse(List<LayoutComponent> layouts) {
		this.layouts = layouts;
	}

	public ProfileLayoutResponse(String code, String description) {
		super(code, description);
	}

	public List<LayoutComponent> getLayouts() {
		return layouts;
	}
}
