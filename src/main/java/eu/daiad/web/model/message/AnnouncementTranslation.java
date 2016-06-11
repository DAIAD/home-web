package eu.daiad.web.model.message;


public class AnnouncementTranslation extends Message {

	private int id;

	private String title;

	private String content;

	private String link;
    
    private Long dispatchedOn;

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.RECOMMENDATION_STATIC;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
    
    public Long getDispatchedOn() {
        return dispatchedOn;
    }

    public void setDispatchedOn(Long dispatchedOn) {
        this.dispatchedOn = dispatchedOn;
    }    

}