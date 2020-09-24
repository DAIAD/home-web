package eu.daiad.common.model.message;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.RestResponse;

public class MultiTypeMessageResponse extends RestResponse
{
	private List<Message> alerts = new ArrayList<>();

	private int totalAlerts = 0;

	private List<Message> recommendations = new ArrayList<>();

	private int totalRecommendations = 0;

	private List<Message> tips = new ArrayList<>();

	private int totalTips = 0;

	private List<Message> announcements = new ArrayList<>();

	private int totalAnnouncements = 0;

	public MultiTypeMessageResponse() {}

	public MultiTypeMessageResponse(MessageResult r)
	{
	    totalAlerts = r.getTotalAlerts();
	    totalRecommendations = r.getTotalRecommendations();
	    totalTips = r.getTotalTips();
	    totalAnnouncements = r.getTotalAnnouncements();

	    for (Message message: r.getMessages()) {
	        switch (message.getType()) {
	        case ALERT:
	            alerts.add(message);
	            break;
	        case ANNOUNCEMENT:
	            announcements.add(message);
	            break;
	        case RECOMMENDATION:
	            recommendations.add(message);
	            break;
	        case TIP:
	            tips.add(message);
	            break;
	        default:
	            // ignore
	            break;
	        }
	    }
	}

	public List<Message> getAlerts() {
		return alerts;
	}

	public List<Message> getRecommendations() {
		return recommendations;
	}

	public List<Message> getTips() {
		return tips;
	}

	public List<Message> getAnnouncements() {
		return announcements;
	}

	public int getTotalAlerts() {
		return totalAlerts;
	}

	public int getTotalRecommendations() {
		return totalRecommendations;
	}

	public int getTotalTips() {
		return totalTips;
	}

	public int getTotalAnnouncements() {
		return totalAnnouncements;
	}
}
