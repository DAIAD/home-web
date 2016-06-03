package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.RestResponse;

public class MultiTypeMessageResponse extends RestResponse {

	private List<Message> alerts = new ArrayList<>();

	private int totalAlerts = 0;

	private List<Message> recommendations = new ArrayList<>();

	private int totalRecommendations = 0;

	private List<Message> tips = new ArrayList<>();

	private int totalTips = 0;

	private List<Message> announcements = new ArrayList<>();

	private int totalAnnouncements = 0;

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

	public int getTotalAlerts() {
		return totalAlerts;
	}

	public void setTotalAlerts(int totalAlerts) {
		this.totalAlerts = totalAlerts;
	}

	public int getTotalRecommendations() {
		return totalRecommendations;
	}

	public void setTotalRecommendations(int totalRecommendations) {
		this.totalRecommendations = totalRecommendations;
	}

	public int getTotalTips() {
		return totalTips;
	}

	public void setTotalTips(int totalTips) {
		this.totalTips = totalTips;
	}

	public int getTotalAnnouncements() {
		return totalAnnouncements;
	}

	public void setTotalAnnouncements(int totalAnnouncements) {
		this.totalAnnouncements = totalAnnouncements;
	}

}
