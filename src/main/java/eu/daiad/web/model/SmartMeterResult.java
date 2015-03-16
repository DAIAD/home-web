package eu.daiad.web.model;

public class SmartMeterResult extends RestResponse{

	private SmartMeterDataPoint value1;
	
	private SmartMeterDataPoint value2;

	private String deviceId;
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public SmartMeterDataPoint getValue1() {
		return value1;
	}

	public void setValue1(SmartMeterDataPoint value) {
		this.value1 = value;
	}
	
	public SmartMeterDataPoint getValue2() {
		return value2;
	}

	public void setValue2(SmartMeterDataPoint value) {
		this.value2 = value;
	}

	public SmartMeterResult() {
		super();
	}

	public SmartMeterResult(int code, String description) {
		super(code, description);
	}
}
