package eu.daiad.web.domain.admin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "daily_counter")
@Table(schema = "public", name = "daily_counter")
public class DailyCounterEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "daily_counter_id_seq", name = "daily_counter_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "daily_counter_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(name = "utility_id")
    private int utilityId;

    @Column(name = "date_created")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn;

    @Column(name = "counter_name")
    private String name;

    @Column(name = "counter_value")
    private long value;

    public int getUtilityId() {
        return utilityId;
    }

    public void setUtilityId(int utilityId) {
        this.utilityId = utilityId;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

}
