package eu.daiad.web.model.message;

public class Alert extends Message {

	private int id;

	private EnumAlertType alert;

	private int priority;

	private String title;

	private String description;

	private String imageLink;

	private Long createdOn;

	public Alert(EnumAlertType alert) {
		this.alert = alert;
	}

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.ALERT;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Long createdOn) {
		this.createdOn = createdOn;
	}

	public String getImageLink() {
		return imageLink;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}

	public EnumAlertType getAlert() {
		return alert;
	}

}
