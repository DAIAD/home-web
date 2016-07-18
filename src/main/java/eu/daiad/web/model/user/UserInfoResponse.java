package eu.daiad.web.model.user;

import java.util.List;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.device.DeviceAmphiroConfiguration;
import eu.daiad.web.model.group.GroupInfo;
import eu.daiad.web.model.meter.WaterMeterStatus;

public class UserInfoResponse extends RestResponse {

    private boolean favorite;

    private UserInfo user;

    private List<DeviceAmphiroConfiguration> configurations;

    private List<WaterMeterStatus> meters;

    private List<AmphiroSessionCollection> devices;

    private List<GroupInfo> groups;

    public UserInfoResponse() {

    }

    public UserInfoResponse(String code, String description) {
        super(code, description);
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public List<WaterMeterStatus> getMeters() {
        return meters;
    }

    public void setMeters(List<WaterMeterStatus> meters) {
        this.meters = meters;
    }

    public List<AmphiroSessionCollection> getDevices() {
        return devices;
    }

    public void setDevices(List<AmphiroSessionCollection> devices) {
        this.devices = devices;
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupInfo> groups) {
        this.groups = groups;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public List<DeviceAmphiroConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<DeviceAmphiroConfiguration> configurations) {
        this.configurations = configurations;
    }

}