package eu.daiad.common.domain.application;


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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_announcement")
@Table(schema = "public", name = "account_announcement")
public class AccountAnnouncementEntity
{
	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "account_announcement_id_seq",
	    name = "account_announcement_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "account_announcement_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne()
	@JoinColumn(name = "account_id", nullable = false)
	@NotNull
	@NaturalId
	private AccountEntity account;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "announcement_id", nullable = false)
	@NotNull
	@NaturalId
	private AnnouncementEntity announcement;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	public AccountAnnouncementEntity() {}

	public AccountAnnouncementEntity(AccountEntity account, AnnouncementEntity announcement)
    {
        this.account = account;
        this.announcement = announcement;
    }

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
