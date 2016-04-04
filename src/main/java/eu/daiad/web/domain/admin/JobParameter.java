package eu.daiad.web.domain.admin;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name = "job_parameter")
@Table(schema = "public", name = "job_parameter")
public class JobParameter {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "job_parameter_id_seq", name = "job_parameter_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "job_parameter_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "job_id", nullable = false)
	private Job job;

	@Basic
	private String name;

	@Basic
	private String value;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getId() {
		return id;
	}

}
