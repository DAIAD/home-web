package eu.daiad.common.model.user;

import java.util.UUID;

import eu.daiad.common.domain.application.DeviceMeterEntity;

public class DeviceMeterInfo {

    private UUID key;

    private String serial;

    public DeviceMeterInfo() {

    }

    public DeviceMeterInfo(DeviceMeterEntity meter) {
        this.key = meter.getKey();
        this.serial = meter.getSerial();
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

}
