package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class AmphiroDevice extends Device {

	private String name;

	public AmphiroDevice(UUID key, String id, String name) {
		super(key, id);

		this.name = name;
	}

	public AmphiroDevice(UUID key, String id, String name,
			ArrayList<KeyValuePair> properties) {
		super(key, id, properties);

		this.name = name;
	}

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

	@Override
	public DeviceRegistration toDeviceRegistration() {
		AmphiroDeviceRegistration r = new AmphiroDeviceRegistration();

		r.setDeviceKey(this.getKey());
		r.setDeviceId(this.getDeviceId());
		r.setName(this.getName());

		for (Iterator<KeyValuePair> p = this.getProperties().iterator(); p
				.hasNext();) {
			KeyValuePair property = p.next();
			r.getProperties().add(
					new KeyValuePair(property.getKey(), property.getValue()));
		}

		return r;
	}
}
