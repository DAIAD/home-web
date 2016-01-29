package eu.daiad.web.model.device;


public class WaterMeterDeviceRegistrationRequest extends
		DeviceRegistrationRequest {

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
