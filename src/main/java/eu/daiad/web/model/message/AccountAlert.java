package eu.daiad.web.model.message;

public class AccountAlert extends Alert {

	private Long acknowledgedOn;

	public AccountAlert(EnumAlertType alert) {
		super(alert);
	}

	public Long getAcknowledgedOn() {
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(Long acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}

}
