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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

@Entity(name = "tip_category")
@Table(schema = "public", name = "tip_category")
public class TipCategoryEntity
{
	@Id()
	@Column(name = "id")
	private int id;

	@Basic()
	@Column(nullable = false)
	@NotNull
	@NaturalId
	private String name;

	@Basic()
    private String title;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "category_id")
	private Set<TipEntity> tips = new HashSet<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<TipEntity> getTips() {
		return tips;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
