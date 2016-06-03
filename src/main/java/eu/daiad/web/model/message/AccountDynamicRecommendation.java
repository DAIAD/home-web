package eu.daiad.web.model.message;

public class AccountDynamicRecommendation extends DynamicRecommendation {

	private Long acknowledgedOn;

	public AccountDynamicRecommendation(EnumDynamicRecommendationType recommendation) {
		super(recommendation);
	}

	public Long getAcknowledgedOn() {
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(Long acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}

}
