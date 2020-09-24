package eu.daiad.common.domain.application;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.common.model.favourite.EnumFavouriteType;

@Entity(name = "favourite")
@Table(schema = "public", name = "favourite")
@Inheritance(strategy = InheritanceType.JOINED)
public class FavouriteEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "favourite_id_seq", name = "favourite_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "favourite_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @Column()
    @Type(type = "pg-uuid")
    private UUID key = UUID.randomUUID();

    @Version()
    @Column(name = "row_version")
    private long rowVersion;

    @ManyToOne()
    @JoinColumn(name = "owner_id", nullable = false)
    private AccountEntity owner;

    @Basic()
    private String label;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    public EnumFavouriteType getType() {
        return EnumFavouriteType.UNDEFINED;
    }

    public AccountEntity getOwner() {
        return owner;
    }

    public void setOwner(AccountEntity owner) {
        this.owner = owner;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public UUID getKey() {
        return key;
    }

    public long getRowVersion() {
        return rowVersion;
    }
}
