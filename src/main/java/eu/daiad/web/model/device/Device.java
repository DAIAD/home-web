package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.web.model.KeyValuePair;

public abstract class Device {

    @JsonIgnore
    private int id;

    private UUID key;

    private ArrayList<KeyValuePair> properties;

    private long registeredOn;

    public Device(int id, UUID key, long registeredOn) {
        this.id = id;
        this.key = key;
        this.properties = new ArrayList<KeyValuePair>();
        this.registeredOn = registeredOn;
    }

    public Device(int id, UUID key, ArrayList<KeyValuePair> properties, long registeredOn) {
        this.id = id;
        this.key = key;
        this.setProperties(properties);
        this.registeredOn = registeredOn;
    }

    public UUID getKey() {
        return key;
    }

    public ArrayList<KeyValuePair> getProperties() {
        return this.properties;
    }

    public void setProperties(ArrayList<KeyValuePair> properties) {
        if (properties == null) {
            this.properties = new ArrayList<KeyValuePair>();
        } else {
            this.properties = properties;
        }
    }

    public abstract EnumDeviceType getType();

    public abstract DeviceRegistration toDeviceRegistration();

    @Override
    public String toString() {
        return "Device [key=" + key + ", properties=" + properties + "]";
    }

    public long getRegisteredOn() {
        return registeredOn;
    }

    public int getId() {
        return id;
    }
}
