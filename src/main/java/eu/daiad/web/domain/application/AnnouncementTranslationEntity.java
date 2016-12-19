package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "announcement_translation")
@Table(schema = "public", name = "announcement_translation")
public class AnnouncementTranslationEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "announcement_translation_id_seq", name = "announcement_translation_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "announcement_translation_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "announcement_id", nullable = false)
	private AnnouncementEntity announcement;

	@Column(name = "locale", columnDefinition = "bpchar", length = 2)
	private String locale;

	@Basic()
	private String title;

	@Basic()
	private String content;

	@Column(name = "link")
	private String link;

    @Column(name = "dispatched_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime dispatchedOn;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
    
	public AnnouncementEntity getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(AnnouncementEntity announcement) {
		this.announcement = announcement;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
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

    public DateTime getDispatchedOn() {
        return dispatchedOn;
    }

    public void setDispatchedOn(DateTime dispatchedOn) {
        this.dispatchedOn = dispatchedOn;
    }

}
