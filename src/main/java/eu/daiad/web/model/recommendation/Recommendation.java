package eu.daiad.web.model.recommendation;

public class Recommendation {

        private String type;
        
	private int id;

	private String title;

	private String description;
        
        private String imageLink;

        public String getType() {
            return type;
	}

	public void setType(String type) {
            this.type = type;
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
        
        public void setImageLink(String imageLink){
            this.imageLink = imageLink;
        }
        
        public String getImageLink(){
            return imageLink;
        }
}
