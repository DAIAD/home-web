package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "dynamic_recommendation")
@Table(schema = "public", name = "dynamic_recommendation")
public class DynamicRecommendationEntity {

	@Id()
	@Column(name = "id")
	private int id;

	@Enumerated(EnumType.STRING)
	private EnumMessageMode mode;

	@Basic()
	private int priority;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "dynamic_recommendation_id")
	private Set<DynamicRecommendationTranslationEntity> translations = new HashSet<DynamicRecommendationTranslationEntity>();

	public EnumMessageMode getMode() {
		return mode;
	}

	public void setMode(EnumMessageMode mode) {
		this.mode = mode;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getId() {
		return id;
	}

	public Set<DynamicRecommendationTranslationEntity> getTranslations() {
		return translations;
	}
}
