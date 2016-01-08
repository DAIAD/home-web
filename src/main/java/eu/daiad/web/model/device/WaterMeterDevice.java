package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;

public class WaterMeterDevice extends Device {

	public WaterMeterDevice(UUID key, String id) {
		super(key, id);
	}

	public WaterMeterDevice(UUID key, String id, ArrayList<KeyValuePair> properties) {
		super(key, id, properties);
	}
	
	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}
	
	@Override
	public DeviceRegistration toDeviceRegistration() {
		WaterMeterDeviceRegistration r = new WaterMeterDeviceRegistration();
		
		r.setDeviceKey(this.getKey());
		r.setDeviceId(this.getDeviceId());
		
		for(Iterator<KeyValuePair> p = this.getProperties().iterator(); p.hasNext(); ) {
			KeyValuePair property = p.next();
		    r.getProperties().add(new KeyValuePair(property.getKey(), property.getValue()));
		}
		
		return r;
	}
}
