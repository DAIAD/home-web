package eu.daiad.common.model.spatial;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.domain.application.DeviceMeterEntity;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.device.WaterMeterDevice;

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
