package eu.daiad.web.model.device;

public class AmphiroDeviceRegistration extends DeviceRegistration {

	private String name;

	private String macAddress;

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

}
