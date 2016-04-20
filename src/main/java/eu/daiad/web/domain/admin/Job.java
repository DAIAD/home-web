package eu.daiad.web.domain.admin;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "job")
@Table(schema = "public", name = "job")
public class Job {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "job_id_seq", name = "job_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "job_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@Column(name = "bean_name")
	private String beanName;

	@Column(name = "job_name")
	private String jobName;

	@Column(name = "job_description")
	private String jobDescription;

	@Column(name = "date_created")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Enumerated(EnumType.STRING)
	private EnumJobCategory category;

	@Enumerated(EnumType.STRING)
	private EnumExecutionContainer container;

	@Basic
	private boolean enabled;

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public int getId() {
		return id;
	}

	public EnumJobCategory getCategory() {
		return category;
	}

	public void setCategory(EnumJobCategory category) {
		this.category = category;
	}

	public EnumExecutionContainer getContainer() {
		return container;
	}

	public void setContainer(EnumExecutionContainer container) {
		this.container = container;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
