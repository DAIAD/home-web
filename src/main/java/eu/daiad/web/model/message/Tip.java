package eu.daiad.web.model.message;

import org.joda.time.DateTime;

public class Tip extends Message
{
	private int index;

	private String locale;

    private String description;

	private byte imageEncoded[];

	private String imageMimeType;

	private String imageLink;

	private String prompt;

	private String externalLink;

	private String source;

	private Long modifiedOn;

	private boolean active;

	public Tip(int id)
	{
	    super(id);
	}

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.TIP;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getLocale()
	{
	    return locale;
	}

	public void setLocale(String locale)
	{
	    this.locale = locale;
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

	public Long getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(DateTime modified)
	{
        this.modifiedOn = modified.getMillis();
    }

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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

    @Override
    public String getBody()
    {
        return description;
    }
}