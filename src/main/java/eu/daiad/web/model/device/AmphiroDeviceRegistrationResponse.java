package eu.daiad.web.model.device;

import java.util.ArrayList;

public class AmphiroDeviceRegistrationResponse extends DeviceRegistrationResponse {

	private ArrayList<DeviceAmphiroConfiguration> configurations = new ArrayList<DeviceAmphiroConfiguration>();

	public AmphiroDeviceRegistrationResponse() {
		super();
	}

	public AmphiroDeviceRegistrationResponse(String code, String description) {
		super(code, description);
	}

	public ArrayList<DeviceAmphiroConfiguration> getConfigurations() {
		return configurations;
	}

}
