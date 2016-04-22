package eu.daiad.web.model.recommendation;


public class RecommendationStatic extends Recommendation {

	private int category;

	private byte image[];

	private String imageLink;

	private String prompt;

	private String externaLink;
        
        private String source;

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
