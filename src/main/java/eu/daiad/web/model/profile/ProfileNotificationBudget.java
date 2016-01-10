package eu.daiad.web.model.profile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ProfileNotificationBudget {

	@JsonDeserialize(using = ProfileNotificationBudgetTypeDeserializer.class)
	private EnumProfileNotificationBudgetType type;

	private float value;

	public EnumProfileNotificationBudgetType getType() {
		return type;
	}

	public void setType(EnumProfileNotificationBudgetType type) {
		this.type = type;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

}
