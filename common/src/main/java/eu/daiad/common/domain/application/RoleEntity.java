package eu.daiad.common.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name = "role")
@Table(schema = "public", name = "role")
public class RoleEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "role_id_seq", name = "role_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "role_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@Basic()
	private String name;

	@Basic()
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getId() {
		return id;
	}
}
