package eu.daiad.web.domain.application;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "announcement_channel")
@Table(schema = "public", name = "announcement_channel")
public class AnnouncementChannel {

	@Id()
	@Column(name = "announcement_id")
	private int announcementId;

    @Basic()
    @Column(name = "channel_id")
    private int channelId;
    
	public int getAnnouncementId() {
		return announcementId;
	}

    public int getChannelId() {
        return channelId;
	}
    
    public void setAnnouncementId(int announcementId) {
        this.announcementId = announcementId;
    }
    
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }


}
