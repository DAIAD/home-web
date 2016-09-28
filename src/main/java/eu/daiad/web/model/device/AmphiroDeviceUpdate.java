package eu.daiad.web.model.device;


public class AmphiroDeviceUpdate extends DeviceUpdate {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public EnumDeviceType getType() {
        return EnumDeviceType.AMPHIRO;
    }

}
