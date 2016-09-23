package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.KeyValuePair;

public class AmphiroDeviceRegistrationResponse extends DeviceRegistrationResponse {

    private String macAddress;

    private String name;

    private String aesKey;

    private List<KeyValuePair> properties = new ArrayList<KeyValuePair>();

    private long registeredOn;

    private ArrayList<DeviceAmphiroConfiguration> configurations = new ArrayList<DeviceAmphiroConfiguration>();

    public AmphiroDeviceRegistrationResponse() {
        super();
    }

    public AmphiroDeviceRegistrationResponse(String code, String description) {
        super(code, description);
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

    public ArrayList<DeviceAmphiroConfiguration> getConfigurations() {
        return configurations;
    }

}
