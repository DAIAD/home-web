package eu.daiad.common.model.message;

public class Announcement extends Message
{
    protected int priority;

	private String content;

	private String link;

	public Announcement()
	{
	    super();
	}

    public Announcement(int id)
    {
        super(id);
    }

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.ANNOUNCEMENT;
	}

	public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

    @Override
    public String getBody()
    {
        return content;
    }
    
//    @Override
//    public Object[] toRowData()
//    {
//        return new Object[] {
//           Integer.valueOf(getId()),
//           getType(),
//           title,
//           content,
//           createdOn,
//           acknowledgedOn
//        };
//    }
//
//    @Override
//    public String[] toRowHeaders()
//    {
//        return new String[] {
//            "Id",
//            "Type",
//            "Title",
//            "Content",
//            "Created",
//            "Acknowledged"
//        };
//    }
    
}
