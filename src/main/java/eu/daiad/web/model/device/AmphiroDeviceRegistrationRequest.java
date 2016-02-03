package eu.daiad.web.model.device;


public class AmphiroDeviceRegistrationRequest extends DeviceRegistrationRequest {

	private String name;

	private String macAddress;

	private String aesKey;

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

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}
}
