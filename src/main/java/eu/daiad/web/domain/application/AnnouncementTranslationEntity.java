package eu.daiad.web.domain.application;

import java.util.Locale;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

@Entity(name = "announcement_translation")
@Table(schema = "public", name = "announcement_translation")
public class AnnouncementTranslationEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "announcement_translation_id_seq",
	    name = "announcement_translation_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "announcement_translation_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "announcement_id", nullable = false)
	@NotNull
	@NaturalId
	private AnnouncementEntity announcement;

	@Column(name = "locale", columnDefinition = "bpchar", length = 2)
	@NotNull
	@NaturalId
	private String locale;

	@Basic()
	@NotNull
	private String title;

	@Basic()
	private String content;

	@Column(name = "link")
	private String link;

	public AnnouncementTranslationEntity() {}

	public AnnouncementTranslationEntity(AnnouncementEntity a, Locale locale, String title, String content)
	{
	    this.announcement = a;
	    this.locale = locale.getLanguage();
	    this.title = title;
	    this.content = content;
	}

	public AnnouncementTranslationEntity(AnnouncementEntity a, Locale locale)
    {
	    this(a, locale, null, null);
    }

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
}
