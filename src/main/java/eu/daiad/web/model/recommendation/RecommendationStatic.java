package eu.daiad.web.model.recommendation;


public class RecommendationStatic extends Recommendation {

	private int category;

	private byte image[];

	private String imageLink;

	private String prompt;

	private String externaLink;

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
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

}
