package eu.daiad.web.model.profile;

public class ProfileNotification {

	private int id;
	
	private boolean active;
	
	private long timestamp;
	
	private ProfileNotificationBudget budget;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public ProfileNotificationBudget getBudget() {
		return budget;
	}

	public void setBudget(ProfileNotificationBudget budget) {
		this.budget = budget;
	}
	
}
