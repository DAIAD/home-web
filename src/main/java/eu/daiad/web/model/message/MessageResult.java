package eu.daiad.web.model.message;

import java.util.List;

public class MessageResult {

	private List<Message> messages;

	private int totalAlerts = 0;

	private int totalRecommendations = 0;

	private int totalTips = 0;

	private int totalAnnouncements = 0;

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
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
