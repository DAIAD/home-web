package eu.daiad.web.domain.application;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

@Entity(name = "area_group_item")
@Table(schema = "public", name = "area_group_item")
public class AreaGroupMemberEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "area_group_item_id", name = "area_group_item_id", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "area_group_item_id", strategy = GenerationType.SEQUENCE)
    private int id;

    @Column()
    @Type(type = "pg-uuid")
    private UUID key = UUID.randomUUID();

    @Version()
    @Column(name = "row_version")
    private long rowVersion;

    @ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "utility_id", nullable = false)
    private UtilityEntity utility;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "area_group_id")
    private AreaGroupEntity group;

    @Basic()
    private String title;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "the_geom")
    private Geometry geometry;

    @Column(name = "level_index")
    private int levelIndex;

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(long rowVersion) {
        this.rowVersion = rowVersion;
    }

    public UtilityEntity getUtility() {
        return utility;
    }

    public void setUtility(UtilityEntity utility) {
        this.utility = utility;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    public void setLevelIndex(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    public int getId() {
        return id;
    }

    public AreaGroupEntity getGroup() {
        return group;
    }

    public void setGroup(AreaGroupEntity group) {
        this.group = group;
    }

}
