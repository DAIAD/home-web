package eu.daiad.web.model.message;

public class AccountAnnoucement extends Announcement {

	private Long acknowledgedOn;

	public Long getAcknowledgedOn() {
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(Long acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}
}
