package eu.daiad.common.model.device;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.AuthenticatedRequest;

public class DeviceUpdateRequest extends AuthenticatedRequest {

    private List<DeviceUpdate> updates = new ArrayList<DeviceUpdate>();

    public List<DeviceUpdate> getUpdates() {
        return updates;
    }

}
