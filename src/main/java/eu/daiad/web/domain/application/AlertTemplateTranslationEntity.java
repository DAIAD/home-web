package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.daiad.web.model.message.EnumAlertTemplate;

@Entity(name = "alert_template_translation")
@Table(
    schema = "public",
    name = "alert_template_translation",
    indexes = {
        @Index(columnList = "template, locale", unique = true),
    }
)
public class AlertTemplateTranslationEntity
{
	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "alert_template_translation_id_seq",
	    name = "alert_template_translation_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "alert_template_translation_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "template", nullable = false)
	@NotNull
    private AlertTemplateEntity template;

	@Column(name = "locale", columnDefinition = "bpchar", length = 2, nullable = false)
	@NotNull
	private String locale;

	@Basic()
	@NotNull
	private String title;

	@Basic()
	private String description;

	@Column(name = "link")
	private String link;

	public AlertTemplateEntity getTemplate() {
		return template;
	}

	public EnumAlertTemplate getTemplateAsEnum() {
        return template.getTemplate();
    }

	public String getLocale() {
		return locale;
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getId() {
		return id;
	}
}
