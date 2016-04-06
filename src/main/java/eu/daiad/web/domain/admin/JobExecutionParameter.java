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

@Entity(name = "job_execution_parameter")
@Table(schema = "public", name = "job_execution_parameter")
public class JobExecutionParameter {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "job_execution_parameter_id_seq", name = "job_execution_parameter_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "job_execution_parameter_id_seq", strategy = GenerationType.SEQUENCE)
	private long id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "job_execution_id", nullable = false)
	private JobExecution execution;

	@Basic
	private String name;

	@Basic
	private String value;

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

	public long getId() {
		return id;
	}

	public JobExecution getExecution() {
		return execution;
	}

	public void setExecution(JobExecution execution) {
		this.execution = execution;
	}

}
