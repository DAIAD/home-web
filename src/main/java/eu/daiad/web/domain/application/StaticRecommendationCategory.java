package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "static_recommendation_category")
@Table(schema = "public", name = "static_recommendation_category")
public class StaticRecommendationCategory {

	@Id()
	@Column(name = "id")
	private int id;

	@Basic()
	private String title;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Set<StaticRecommendation> properties = new HashSet<StaticRecommendation>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<StaticRecommendation> getProperties() {
		return properties;
	}

	public void setProperties(Set<StaticRecommendation> properties) {
		this.properties = properties;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
