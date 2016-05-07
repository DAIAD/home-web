package eu.daiad.web.model.message;

public class StaticRecommendation extends Message {

	private int id;

	private String title;

	private String description;

	private byte imageEncoded[];

	private String imageLink;

	private String prompt;

	private String externaLink;

	private String source;

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.RECOMMENDATION_STATIC;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getExternaLink() {
		return externaLink;
	}

	public void setExternaLink(String externaLink) {
		this.externaLink = externaLink;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}