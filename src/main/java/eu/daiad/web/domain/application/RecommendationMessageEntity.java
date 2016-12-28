package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.EnumRecommendationType;

@Entity(name = "recommendation_message")
@Table(
    schema = "public",
    name = "recommendation_message",
    indexes = {
        @Index(columnList = "template_name, locale", unique = true),
    }
)
public class RecommendationMessageEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "recommendation_message_id_seq",
	    name = "recommendation_message_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "recommendation_message_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "recommendation_id", nullable = false)
    private RecommendationTypeEntity type;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "template_name")
	private EnumRecommendationTemplate template;

	@Column(name = "locale", columnDefinition = "bpchar", length = 2)
	private String locale;

	@Basic()
	private String title;

	@Basic()
	private String description;

	@Column(name = "image_link")
	private String imageLink;

	public EnumRecommendationTemplate getTemplate() {
		return template;
	}
	
	public EnumRecommendationType getRecommendationType() {
        return template.getType();
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
