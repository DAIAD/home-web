package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Locale;
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
public class AlertEntity {

	@Id()
	@Column(name = "id")
	private int id;

	@Enumerated(EnumType.STRING)
	private EnumMessageMode mode;

	@Basic()
	private int priority;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "alert_id")
	private Set<AlertTranslationEntity> translations = new HashSet<>();

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

	public Set<AlertTranslationEntity> getTranslations() 
	{
		return translations;
	}

	public AlertTranslationEntity getTranslation(Locale locale) 
	{
	    AlertTranslationEntity result = null;
	    String langCode = locale.getLanguage();
	    for (AlertTranslationEntity t: translations) {
	        if (t.getLocale().equals(langCode)) {
	            result = t;
	            break;
	        }
	    }
	    return result;
	}
}
