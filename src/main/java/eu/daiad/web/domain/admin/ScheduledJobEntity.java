package eu.daiad.web.domain.admin;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "scheduled_job")
@Table(schema = "public", name = "scheduled_job")
public class ScheduledJobEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "scheduled_job_id_seq", name = "scheduled_job_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "scheduled_job_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @Enumerated(EnumType.STRING)
    private EnumJobCategory category;

    @Enumerated(EnumType.STRING)
    private EnumExecutionContainer container;

    @Basic
    private String bean;

    @Basic
    private String name;

    @Basic
    private String description;

    @Column(name = "date_created")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn;

    @Basic
    private Long period;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Basic
    private boolean enabled;

    @Basic
    private boolean visible;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinColumn(name = "scheduled_job_id")
    private Set<ScheduledJobParameterEntity> parameters = new HashSet<ScheduledJobParameterEntity>();

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

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

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

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getId() {
        return id;
    }

    public Set<ScheduledJobParameterEntity> getParameters() {
        return parameters;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
