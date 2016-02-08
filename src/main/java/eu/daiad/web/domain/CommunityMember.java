package eu.daiad.web.domain;

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

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "community_member")
@Table(schema = "public", name = "community_member")
public class CommunityMember {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "community_member_id_seq", name = "community_member_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "community_member_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "community_id", nullable = false)
	private Community community;

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createtOn = new DateTime();

	@Basic()
	private int ranking;

	public Community getCommunity() {
		return community;
	}

	public void setCommunity(Community community) {
		this.community = community;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public DateTime getCreatetOn() {
		return createtOn;
	}

	public void setCreatetOn(DateTime createtOn) {
		this.createtOn = createtOn;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public int getId() {
		return id;
	}

}
