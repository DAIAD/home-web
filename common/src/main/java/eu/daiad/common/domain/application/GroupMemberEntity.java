package eu.daiad.common.domain.application;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "group_member")
@SqlResultSetMapping(name="WaterIqResult", classes = {
    @ConstructorResult(targetClass = eu.daiad.common.domain.application.mappings.GroupMemberWaterIq.class, columns = {
        @ColumnResult(name="id"),
        @ColumnResult(name="volume", type = Double.class),
        @ColumnResult(name="value")
    })
})
@Table(schema = "public", name = "group_member")
public class GroupMemberEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "group_member_id_seq", name = "group_member_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "group_member_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "group_id", nullable = false)
	private GroupEntity group;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createtOn = new DateTime();

	public GroupEntity getGroup() {
		return group;
	}

	public void setGroup(GroupEntity group) {
		this.group = group;
	}

	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public DateTime getCreatetOn() {
		return createtOn;
	}

	public void setCreatetOn(DateTime createtOn) {
		this.createtOn = createtOn;
	}

}
