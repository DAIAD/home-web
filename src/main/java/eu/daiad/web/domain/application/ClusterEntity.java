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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.group.EnumGroupType;

@Entity(name = "cluster")
@Table(schema = "public", name = "cluster")
public class ClusterEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "cluster_id_seq", name = "cluster_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "cluster_id_seq", strategy = GenerationType.SEQUENCE)
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

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumn(name = "cluster_id")
	private Set<GroupSegmentEntity> groups = new HashSet<GroupSegmentEntity>();

	@Basic()
	private String name;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn = new DateTime();

	public int getId() {
		return id;
	}

	public UtilityEntity getUtility() {
		return utility;
	}

	public void setUtility(UtilityEntity utility) {
		this.utility = utility;
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

	public long getRowVersion() {
		return rowVersion;
	}

	public UUID getKey() {
		return key;
	}

	public EnumGroupType getType() {
		return EnumGroupType.UNDEFINED;
	}

	public Set<GroupSegmentEntity> getGroups() {
		return groups;
	}

}
