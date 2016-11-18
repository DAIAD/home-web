package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.UUID;

public class DeviceAmphiroConfiguration {

    private UUID version;

    private UUID deviceKey;
    
    private String title;

    private long createdOn;

    private Long acknowledgedOn;

    private Long enabledOn;

    private ArrayList<Integer> properties = new ArrayList<Integer>();

    private int block;

    private int numberOfFrames;

    private int frameDuration;

    public DeviceAmphiroConfiguration(eu.daiad.web.domain.application.DeviceAmphiroConfigurationEntity c) {
        this.deviceKey = c.getDevice().getKey();
        
        this.title = c.getTitle();
        this.createdOn = c.getCreatedOn().getMillis();

        this.properties.add(c.getValue1());
        this.properties.add(c.getValue2());
        this.properties.add(c.getValue3());
        this.properties.add(c.getValue4());
        this.properties.add(c.getValue5());
        this.properties.add(c.getValue6());
        this.properties.add(c.getValue7());
        this.properties.add(c.getValue8());
        this.properties.add(c.getValue9());
        this.properties.add(c.getValue10());
        this.properties.add(c.getValue11());
        this.properties.add(c.getValue12());

        this.block = c.getBlock();
        this.frameDuration = c.getFrameDuration();
        this.numberOfFrames = c.getNumberOfFrames();

        this.version = c.getVersion();
        if (c.getAcknowledgedOn() != null) {
            this.acknowledgedOn = c.getAcknowledgedOn().getMillis();
        }
        if (c.getEnabledOn() != null) {
            this.enabledOn = c.getEnabledOn().getMillis();
        }

    }

    public ArrayList<Integer> getProperties() {
        return properties;
    }

    public void add(int value) {
        this.properties.add(value);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public int getFrameDuration() {
        return frameDuration;
    }

    public void setFrameDuration(int frameDuration) {
        this.frameDuration = frameDuration;
    }

    public void setProperties(ArrayList<Integer> properties) {
        this.properties = properties;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public UUID getVersion() {
        return version;
    }

    public void setVersion(UUID version) {
        this.version = version;
    }

    public Long getAcknowledgedOn() {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(Long acknowledgedOn) {
        this.acknowledgedOn = acknowledgedOn;
    }

    public Long getEnabledOn() {
        return enabledOn;
    }

    public void setEnabledOn(Long enabledOn) {
        this.enabledOn = enabledOn;
    }

    public UUID getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(UUID deviceKey) {
        this.deviceKey = deviceKey;
    }

}
