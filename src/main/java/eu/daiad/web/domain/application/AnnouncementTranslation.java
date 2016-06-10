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

@Entity(name = "announcement_translation")
@Table(schema = "public", name = "announcement_translation")
public class AnnouncementTranslation {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "announcement_translation_id_seq", name = "announcement_translation_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "announcement_translation_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "announcement_id", nullable = false)
	private Announcement announcement;

	@Column(name = "locale", columnDefinition = "bpchar", length = 2)
	private String locale;

	@Basic()
	private String title;

	@Basic()
	private String content;

	@Column(name = "link")
	private String imageLink;

	public Announcement getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(Announcement announcement) {
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

	public String getImageLink() {
		return imageLink;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}

	public int getId() {
		return id;
	}

}
