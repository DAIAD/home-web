package eu.daiad.web.model.spatial;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.domain.application.DeviceMeterEntity;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.device.WaterMeterDevice;

public class MeterCollectionResponse extends RestResponse {

    private List<WaterMeterDevice> meters = new ArrayList<WaterMeterDevice>();

    public MeterCollectionResponse(List<DeviceMeterEntity> entities) {
        if (entities != null) {
            for (DeviceMeterEntity entity : entities) {
                meters.add(new WaterMeterDevice(entity));
            }
        }
    }

    public List<WaterMeterDevice> getMeters() {
        return meters;
    }

}
