package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.RestResponse;

public class MultiTypeMessageResponse extends RestResponse {

	private List<Message> alerts = new ArrayList<>();

	private List<Message> recommendations = new ArrayList<>();

	private List<Message> tips = new ArrayList<>();

	private List<Message> announcements = new ArrayList<>();

	public List<Message> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<Message> alerts) {
		this.alerts = alerts;
	}

	public List<Message> getRecommendations() {
		return recommendations;
	}

	public void setRecommendations(List<Message> recommendations) {
		this.recommendations = recommendations;
	}

	public List<Message> getTips() {
		return tips;
	}

	public void setTips(List<Message> tips) {
		this.tips = tips;
	}

	public List<Message> getAnnouncements() {
		return announcements;
	}

	public void setAnnouncements(List<Message> announcements) {
		this.announcements = announcements;
	}

}
