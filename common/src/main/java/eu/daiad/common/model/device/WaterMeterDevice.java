package eu.daiad.common.model.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import eu.daiad.common.domain.application.DeviceMeterEntity;
import eu.daiad.common.model.KeyValuePair;

public class WaterMeterDevice extends Device {

    private String serial;

    private Geometry location;

    public WaterMeterDevice(DeviceMeterEntity entity) {
        this(entity.getId(),
             entity.getKey(),
             entity.getSerial(),
             entity.getLocation(),
             entity.getRegisteredOn().getMillis());
    }

    public WaterMeterDevice(int id, UUID key, String serial, long registeredOn) {
        super(id, key, registeredOn);

        this.serial = serial;
    }

    public WaterMeterDevice(int id, UUID key, String serial, ArrayList<KeyValuePair> properties, long registeredOn) {
        super(id, key, properties, registeredOn);

        this.serial = serial;
    }

    public WaterMeterDevice(int id, UUID key, String serial, Geometry location, long registeredOn) {
        super(id, key, registeredOn);

        this.serial = serial;
        this.location = location;
    }

    public WaterMeterDevice(int id, UUID key, String serial, ArrayList<KeyValuePair> properties, Geometry location,
                    long registeredOn) {
        super(id, key, properties, registeredOn);

        this.serial = serial;
        this.location = location;
    }

    @Override
    public EnumDeviceType getType() {
        return EnumDeviceType.METER;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Override
    public DeviceRegistration toDeviceRegistration() {
        WaterMeterDeviceRegistration r = new WaterMeterDeviceRegistration();

        r.setDeviceKey(getKey());
        r.setSerial(getSerial());
        r.setRegisteredOn(getRegisteredOn());

        for (Iterator<KeyValuePair> p = getProperties().iterator(); p.hasNext();) {
            KeyValuePair property = p.next();
            r.getProperties().add(new KeyValuePair(property.getKey(), property.getValue()));
        }

        return r;
    }

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }
}
