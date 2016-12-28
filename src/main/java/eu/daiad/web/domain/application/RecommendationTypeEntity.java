package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.EnumRecommendationType;

@Entity(name = "recommendation_type")
@Table(schema = "public", name = "recommendation_type")
public class RecommendationTypeEntity 
{
	@Id()
	private int id;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "name")
	private EnumRecommendationType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "device", nullable = true)
	private EnumDeviceType deviceType;

	@Basic()
	private int priority;

	public EnumDeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(EnumDeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public EnumRecommendationType getType() {
		return type;
	}
	
	public int getId() {
	    return id;
	}
}
