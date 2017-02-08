package eu.daiad.web.domain.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "alert_resolver_execution")
@Table(schema = "public", name = "alert_resolver_execution")
public class AlertResolverExecutionEntity
{
    @Id()
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "alert_resolver_execution_id_seq",
        name = "alert_resolver_execution_id_seq",
        allocationSize = 1,
        initialValue = 1)
    @GeneratedValue(generator = "alert_resolver_execution_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;
    
    @Column(name = "resolver_name")
    @NotNull
    private String resolverName;

    @Column(name = "ref_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @NotNull
    private DateTime refDate;
    
    @Column(name = "started")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @NotNull
    private DateTime started;

    @Column(name = "finished")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime finished;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target", nullable = true)
    private UtilityEntity target;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_group", nullable = true)
    private GroupEntity targetGroup;
    
    public AlertResolverExecutionEntity()
    {}
    
    public AlertResolverExecutionEntity(DateTime refDate, String resolverName, UtilityEntity target)
    {
        this.refDate = refDate;
        this.resolverName = resolverName;
        this.target = target;
        this.targetGroup = null;
    }
    
    public AlertResolverExecutionEntity(DateTime refDate, String resolverName, GroupEntity target)
    {
        this.refDate = refDate;
        this.resolverName = resolverName;
        this.target = null;
        this.targetGroup = target;
    }
    
    public AlertResolverExecutionEntity(DateTime refDate, String resolverName)
    {
        this(refDate, resolverName, (UtilityEntity) null);
    }

    public int getId()
    {
        return id;
    }
    
    public DateTime getStarted()
    {
        return started;
    }

    public void setStarted(DateTime started)
    {
        this.started = started;
    }

    public DateTime getFinished()
    {
        return finished;
    }

    public void setFinished(DateTime finished)
    {
        this.finished = finished;
    }
    public String getResolverName()
    {
        return resolverName;
    }

    public DateTime getRefDate()
    {
        return refDate;
    }

    public UtilityEntity getTarget()
    {
        return target;
    }

    public GroupEntity getTargetGroup()
    {
        return targetGroup;
    }
}

