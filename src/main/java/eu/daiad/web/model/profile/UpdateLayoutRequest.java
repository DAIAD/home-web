package eu.daiad.web.model.profile;

import eu.daiad.web.model.AuthenticatedRequest;
import java.util.List;

public class UpdateLayoutRequest extends AuthenticatedRequest {

    private List<LayoutComponent> layouts;

    public List<LayoutComponent> getLayouts() {
        return layouts;
    }

    public void setLayouts(List<LayoutComponent> layouts) {
        this.layouts = layouts;
    }

}
