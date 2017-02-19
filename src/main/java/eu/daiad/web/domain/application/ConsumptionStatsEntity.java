package eu.daiad.web.domain.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.springframework.data.util.Pair;

import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.query.EnumDataField;

@Entity(name = "consumption_stats")
@Table(schema = "public", name = "consumption_stats",
        indexes = {
            @Index(columnList = "`group`, utility, ref_date", unique = false),
        }
)
public class ConsumptionStatsEntity
{
    @Id()
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "consumption_stats_id_seq",
        name = "consumption_stats_id_seq",
        allocationSize = 1,
        initialValue = 1)
    @GeneratedValue(generator = "consumption_stats_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne
    @JoinColumn(name = "`group`", nullable = true)
    private GroupEntity group;

    @ManyToOne
    @JoinColumn(name = "utility", nullable = false)
    private UtilityEntity utility;

    @Column(name = "ref_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime refDate;

    @Column(name = "statistic", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnumStatistic statistic = EnumStatistic.AVERAGE_WEEKLY;

    @Column(name = "field",  nullable = false)
    @Enumerated(EnumType.STRING)
    private EnumDataField field = EnumDataField.VOLUME;

    @Column(name = "device",  nullable = false)
    @Enumerated(EnumType.STRING)
    private EnumDeviceType device = EnumDeviceType.METER;

    @Column(name = "value",  nullable = false)
    private double value;

    @Column(name = "computed_at")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime computedAt;

    protected ConsumptionStatsEntity() {}

    public ConsumptionStatsEntity(UtilityEntity utility, GroupEntity group, DateTime refDate)
    {
        this.utility = utility;
        this.group = group;
        this.refDate = refDate;
    }

    public ConsumptionStatsEntity(UtilityEntity utility, GroupEntity group, LocalDateTime refDate)
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

    public DateTime getRefDate()
    {
        return refDate;
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

    public EnumDeviceType getDevice()
    {
        return device;
    }

    public void setDevice(EnumDeviceType device)
    {
        this.device = device;
    }

    public ComputedNumber getValue()
    {
        return ComputedNumber.valueOf(value, computedAt);
    }

    public void setValue(ComputedNumber n)
    {
        this.value = n.getValue();
        this.computedAt = n.getTimestamp();
    }

    @Override
    public String toString()
    {
        return String.format(
            "ConsumptionStats(ref-date=%s utility=%s device=%s stat=%s)",
            refDate.toDate(), utility.getKey(), device.name(), statistic.name()
        );
    }
}
