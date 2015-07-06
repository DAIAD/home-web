package eu.daiad.web.model;

public class DeviceRegistrationResponse extends RestResponse {

	private String deviceKey;
	
	public DeviceRegistrationResponse() {
		super();
	}

	public DeviceRegistrationResponse(int code, String description) {
		super(code, description);
	}
	
    public String getDeviceKey() {
    	return this.deviceKey;
    }
    
    public void setDeviceKey(String value) {
    	this.deviceKey = value;
    }
}
