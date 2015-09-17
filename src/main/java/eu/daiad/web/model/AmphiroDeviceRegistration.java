package eu.daiad.web.model;

public class AmphiroDeviceRegistration extends DeviceRegistration {

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
