package eu.daiad.common.model.device;

public class WaterMeterDeviceRegistration extends DeviceRegistration {

	private String serial;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}

}
