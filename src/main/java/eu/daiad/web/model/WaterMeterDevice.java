package eu.daiad.web.model;

import java.util.UUID;

public class WaterMeterDevice extends Device {

	public WaterMeterDevice(String id, UUID key, String name) {
		super(id, key, name);
	}

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}
	
}
