package eu.daiad.web.domain.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.springframework.data.util.Pair;

import eu.daiad.web.model.ConsumptionStats.EnumStatistic;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.query.EnumDataField;

@Entity(name = "utility_statistics")
@Table(
    schema = "public", name = "utility_statistics",
    indexes = {
        @Index(columnList = "utility, ref_date", unique = false),
    }
)
public class UtilityStatisticsEntity
{
    @Id()
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "utility_statistics_id_seq",
        name = "utility_statistics_id_seq",
        allocationSize = 1,
        initialValue = 1)
    @GeneratedValue(generator = "utility_statistics_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;
    
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "`group`", nullable = true, updatable = false)
    private GroupEntity group;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "utility", nullable = false, updatable = false)
    private UtilityEntity utility;

    @Column(name = "ref_date", nullable = false, updatable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime refDate;

    @Column(name = "period", nullable = false, updatable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentPeriodAsString")
    private Period period;
    
    @Column(name = "statistic", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EnumStatistic statistic;

    @Column(name = "field",  nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EnumDataField field = EnumDataField.VOLUME;

    @Column(name = "device_type",  nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EnumDeviceType deviceType = EnumDeviceType.METER;

    @Column(name = "value",  nullable = false)
    private double value;

    @Column(name = "computed_at")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime computedAt;

    protected UtilityStatisticsEntity() {}

    public UtilityStatisticsEntity(UtilityEntity utility, GroupEntity group, DateTime refDate)
    {
        this.utility = utility;
        this.group = group;
        this.refDate = refDate;
    }

    public UtilityStatisticsEntity(UtilityEntity utility, GroupEntity group, LocalDateTime refDate)
    {
        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());
        this.utility = utility;
        this.group = group;
        this.refDate = refDate.toDateTime(tz);
    }

    public Pair<UtilityEntity, GroupEntity> getPopulationGroup()
    {
        return Pair.<UtilityEntity, GroupEntity>of(utility, group);
    }

    public void setPopulationGroup(UtilityEntity utility, GroupEntity group)
    {
        this.utility = utility;
        this.group = group;
    }
    
    public EnumStatistic getStatistic()
    {
        return statistic;
    }

    public void setStatistic(EnumStatistic statistic)
    {
        this.statistic = statistic;
    }

    public EnumDataField getField()
    {
        return field;
    }

    public void setField(EnumDataField field)
    {
        this.field = field;
    }

    public EnumDeviceType getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(EnumDeviceType deviceType)
    {
        this.deviceType = deviceType;
    }

    public Period getPeriod()
    {
        return period;
    }

    public void setPeriod(Period period)
    {
        this.period = period;
    }

    public double getValue()
    {
        return value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }

    public DateTime getComputedAt()
    {
        return computedAt;
    }

    public void setComputedAt(DateTime computedAt)
    {
        this.computedAt = computedAt;
    }

    public void setRefDate(DateTime refDate)
    {
        this.refDate = refDate;
    }

    public DateTime getRefDate()
    {
        return refDate;
    }
}
