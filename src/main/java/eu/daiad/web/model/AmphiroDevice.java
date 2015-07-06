package eu.daiad.web.model;

import java.util.UUID;

public class AmphiroDevice extends Device {

	public AmphiroDevice(String id, UUID key, String name) {
		super(id, key, name);
	}

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.AMPHIRO;
	}
	
}
