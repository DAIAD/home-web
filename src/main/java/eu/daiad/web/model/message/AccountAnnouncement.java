package eu.daiad.web.model.message;

public class AccountAnnouncement extends Announcement {

	private Long acknowledgedOn;
    
	public AccountAnnouncement() {
		super();
	}
    
	public Long getAcknowledgedOn() {
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(Long acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}
}
