package eu.daiad.web.model;

public class WaterMeterDeviceRegistration extends DeviceRegistration {

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}

}
