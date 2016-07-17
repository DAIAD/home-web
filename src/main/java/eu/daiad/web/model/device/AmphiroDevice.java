package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;

public class AmphiroDevice extends Device {

    private String name;

    private String macAddress;

    private String aesKey;

    private DeviceAmphiroConfiguration configuration;

    public AmphiroDevice(int id, UUID key, String name, String macAddress, String aesKey, long registeredOn) {
        super(id, key, registeredOn);

        this.name = name;
        this.macAddress = macAddress;
        this.aesKey = aesKey;
    }

    public AmphiroDevice(int id, UUID key, String name, String macAddress, ArrayList<KeyValuePair> properties,
                    long registeredOn) {
        super(id, key, properties, registeredOn);

        this.name = name;
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public EnumDeviceType getType() {
        return EnumDeviceType.AMPHIRO;
    }

    @Override
    public DeviceRegistration toDeviceRegistration() {
        AmphiroDeviceRegistration r = new AmphiroDeviceRegistration();

        r.setDeviceKey(this.getKey());
        r.setName(this.getName());
        r.setMacAddress(this.getMacAddress());
        r.setAesKey(this.getAesKey());
        r.setRegisteredOn(this.getRegisteredOn());

        for (Iterator<KeyValuePair> p = this.getProperties().iterator(); p.hasNext();) {
            KeyValuePair property = p.next();
            r.getProperties().add(new KeyValuePair(property.getKey(), property.getValue()));
        }

        return r;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public DeviceAmphiroConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(DeviceAmphiroConfiguration configuration) {
        this.configuration = configuration;
    }

}
