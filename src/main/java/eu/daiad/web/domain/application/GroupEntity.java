package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.model.group.EnumGroupType;

@Entity(name = "group")
@Table(schema = "public", name = "group")
@Inheritance(strategy = InheritanceType.JOINED)
public class GroupEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "group_id_seq", name = "group_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "group_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @Column()
    @Type(type = "pg-uuid")
    private UUID key = UUID.randomUUID();

    @Version()
    @Column(name = "row_version")
    private long rowVersion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utility_id", nullable = false)
    private UtilityEntity utility;

    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Set<GroupMemberEntity> members = new HashSet<GroupMemberEntity>();

    @Basic()
    private String name;

    @Basic()
    private int size;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "updated_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updatedOn = new DateTime();

    @Column(name = "spatial")
    private Geometry geometry;

    public int getId() {
        return id;
    }

    public UtilityEntity getUtility() {
        return utility;
    }

    public void setUtility(UtilityEntity utility) {
        this.utility = utility;
    }

    public Set<GroupMemberEntity> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getRowVersion() {
        return rowVersion;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public EnumGroupType getType() {
        return EnumGroupType.UNDEFINED;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public DateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(DateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

}
