package eu.daiad.web.domain.application;

import eu.daiad.web.model.message.EnumMessageType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "alert_analytics")
public class AlertAnalytics {
    
	@Id()
	@Column(name = "id")
	private Integer id;    
    
	@Column(name = "title")
	private String title;  
    
	@Column(name = "description")
	private String description;     

	@Column(name = "locale", columnDefinition = "bpchar", length = 2)
	private String locale;
    
	@Column(name = "total")
	private long receiversCount;   

    public EnumMessageType getType() {
        return EnumMessageType.ALERT;
    }   
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
    
    public String getLocale() {
        return locale;
    } 

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public long getReceiversCount() {
        return receiversCount;
    }

    public void setReceiversCount(long receiversCount) {
        this.receiversCount = receiversCount;
    }
    
}
