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

@Entity(name = "alert")
@Table(schema = "public", name = "alert")
public class Alert {

	@Id()
	@Column(name = "id")
	private int id;

	@Enumerated(EnumType.STRING)
	private EnumMessageMode mode;

	@Basic()
	private int priority;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "alert_id")
	private Set<AlertTranslation> translations = new HashSet<AlertTranslation>();

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getId() {
		return id;
	}

	public EnumMessageMode getMode() {
		return mode;
	}

	public Set<AlertTranslation> getTranslations() {
		return translations;
	}

}
