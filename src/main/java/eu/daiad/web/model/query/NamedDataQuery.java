package eu.daiad.web.model.query;

import org.joda.time.DateTime;

public class NamedDataQuery {

	public enum EnumFavouriteType {
		UNDEFINED, MAP, CHART;
	}  

	public NamedDataQuery() {
		this.type = EnumQueryFavouriteType.UNDEFINED;
	}    
    
	private EnumQueryFavouriteType type;
    
    private long id;

    private String title;

    private String tags;
    
    private String reportName;
    
    private String level;    
    
    private String field;        

    private DateTime createdOn;
    
    private DataQuery query;

    public EnumQueryFavouriteType getType() {
        return type;
    }
    
    public void setType(EnumQueryFavouriteType type) {
        this.type = type;
    } 
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DataQuery getQuery() {
        return query;
    }

    public void setQuery(DataQuery query) {
        this.query = query;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
    
    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }
}
