package eu.daiad.web.domain.application;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_announcement")
@Table(schema = "public", name = "account_announcement")
public class AccountAnnouncementEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_announcement_id_seq", name = "account_announcement_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_announcement_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "announcement_id", nullable = false)
	private AnnouncementEntity announcement;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public AnnouncementEntity getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(AnnouncementEntity announcement) {
		this.announcement = announcement;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public DateTime getAcknowledgedOn() {
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(DateTime acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}

	public int getId() {
		return id;
	}

}
