package eu.daiad.web.domain.application;

import javax.persistence.Basic;
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

import eu.daiad.web.model.message.EnumRecommendationTemplate;

@Entity(name = "recommendation_template_translation")
@Table(
    schema = "public",
    name = "recommendation_template_translation",
    indexes = {
        @Index(columnList = "template, locale", unique = true),
    }
)
public class RecommendationTemplateTranslationEntity
{
	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "recommendation_template_translation_id_seq",
	    name = "recommendation_template_translation_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "recommendation_template_translation_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne()
    @JoinColumn(name = "template", nullable = false)
	@NotNull
    private RecommendationTemplateEntity template;

	@Column(name = "locale", columnDefinition = "bpchar", length = 2, nullable = false)
	@NotNull
	private String locale;

	@Basic()
	@NotNull
	private String title;

	@Basic()
	private String description;

	@Column(name = "image_link")
	private String imageLink;

	public RecommendationTemplateEntity getTemplate() {
		return template;
	}

	public EnumRecommendationTemplate getTemplateAsEnum() {
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
