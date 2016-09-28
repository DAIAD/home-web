package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.AuthenticatedRequest;

public class DeviceUpdateRequest extends AuthenticatedRequest {

    private List<DeviceUpdate> updates = new ArrayList<DeviceUpdate>();

    public List<DeviceUpdate> getUpdates() {
        return updates;
    }

}
