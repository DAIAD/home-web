package eu.daiad.web.domain.application;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

@Entity(name = "announcement_channel")
@Table(schema = "public", name = "announcement_channel")
public class AnnouncementChannelEntity
{
	@Id()
	@Column(name = "id")
    @SequenceGenerator(
        sequenceName = "announcement_channel_id_seq",
        name = "announcement_channel_id_seq",
        allocationSize = 1,
        initialValue = 1)
    @GeneratedValue(generator = "announcement_channel_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

	@ManyToOne
    @JoinColumn(name = "announcement_id", nullable = false)
	@NotNull
	@NaturalId
	private AnnouncementEntity announcement;

	@ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    @NotNull
    @NaturalId
    private ChannelEntity channel;

    public AnnouncementChannelEntity() {}

    public AnnouncementChannelEntity(AnnouncementEntity a, ChannelEntity c)
    {
        this.announcement = a;
        this.channel = c;
    }

	public AnnouncementEntity getAnnouncement() {
		return announcement;
	}

    public ChannelEntity getChannel() {
        return channel;
	}
}
