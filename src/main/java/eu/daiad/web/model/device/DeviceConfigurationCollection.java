package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;

public class DeviceConfigurationCollection {

    private UUID key;

    private String macAddress;

    private String name;

    private String aesKey;

    private List<KeyValuePair> properties = new ArrayList<KeyValuePair>();

    private long registeredOn;

    private List<DeviceAmphiroConfiguration> configurations = new ArrayList<DeviceAmphiroConfiguration>();

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public long getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(long registeredOn) {
        this.registeredOn = registeredOn;
    }

    public List<KeyValuePair> getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        for (KeyValuePair property : this.properties) {
            if (property.getKey().equals(key)) {
                return property.getValue();
            }
        }

        return null;
    }

    public List<DeviceAmphiroConfiguration> getConfigurations() {
        return configurations;
    }

}
