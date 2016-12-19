package eu.daiad.web.model.message;

import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;

public class StaticRecommendation extends Message 
{
    public interface Parameters extends Message.Parameters {}
    
    public abstract static class AbstractParameters extends Message.AbstractParameters implements Parameters
    {
        protected AbstractParameters(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }
    }
    
	private int id;

	private int index;

	private String title;

	private String description;

	private byte imageEncoded[];

	private String imageMimeType;

	private String imageLink;

	private String prompt;

	private String externalLink;

	private String source;

	private Long createdOn;

	private Long modifiedOn;

	private boolean active;

	private Long acknowledgedOn;
	
	@Override
	public EnumMessageType getType() {
		return EnumMessageType.RECOMMENDATION_STATIC;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public byte[] getImageEncoded() {
		return imageEncoded;
	}

	public void setImageEncoded(byte[] imageEncoded) {
		this.imageEncoded = imageEncoded;
	}

	public String getImageLink() {
		return imageLink;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Long createdOn) {
		this.createdOn = createdOn;
	}

	public Long getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Long modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getExternalLink() {
		return externalLink;
	}

	public void setExternalLink(String externalLink) {
		this.externalLink = externalLink;
	}

	public String getImageMimeType() {
		return imageMimeType;
	}

	public void setImageMimeType(String imageMimeType) {
		this.imageMimeType = imageMimeType;
	}

	public Long getAcknowledgedOn() {
	    return acknowledgedOn;
	}

	public void setAcknowledgedOn(Long acknowledgedOn) {
	    this.acknowledgedOn = acknowledgedOn;
	}
}