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

@Entity(name = "announcement")
@Table(schema = "public", name = "announcement")
public class Announcement {

	@Id()
	@Column(name = "id")
	private int id;

	@Basic()
	private int priority;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "announcement_id")
	private Set<AnnouncementTranslation> translations = new HashSet<AnnouncementTranslation>();

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getId() {
		return id;
	}
    
    public void setId(int id) {
        this.id = id;
    }

	public Set<AnnouncementTranslation> getTranslations() {
		return translations;
	}

}
