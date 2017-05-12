package eu.daiad.web.domain.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Entity(name = "announcement")
@Table(schema = "public", name = "announcement")
public class AnnouncementEntity
{
	@Id()
	@Column(name = "id")
	@SequenceGenerator(
        sequenceName = "announcement_id_seq",
        name = "announcement_id_seq",
        allocationSize = 1,
        initialValue = 1)
    @GeneratedValue(generator = "announcement_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@Basic()
	private int priority = 5;

	@OneToMany(
	    mappedBy = "announcement",
	    cascade = { CascadeType.ALL },
	    fetch = FetchType.EAGER,
	    orphanRemoval = true
	)
	private Set<AnnouncementTranslationEntity> translations = new HashSet<>();

	@OneToMany(
	    mappedBy = "channel",
	    cascade = { CascadeType.ALL },
	    fetch = FetchType.EAGER,
	    orphanRemoval = true
	)
    private Set<AnnouncementChannelEntity> channels = new HashSet<>();

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = DateTime.now(DateTimeZone.UTC);

	public AnnouncementEntity() {}

	public AnnouncementEntity(int priority)
	{
	    this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getId() {
		return id;
	}

    public void setId(int id) {
        this.id = id;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public List<ChannelEntity> getChannels()
    {
        List<ChannelEntity> result = new ArrayList<>();
        for (AnnouncementChannelEntity ac: channels)
            result.add(ac.getChannel());
        return result;
    }

    public boolean directsTo(String channelName)
    {
        for (AnnouncementChannelEntity ac: channels)
            if (ac.getChannel().getName().equalsIgnoreCase(channelName))
                return true;
        return false;
    }

    public boolean addChannel(ChannelEntity channel)
    {
        if (directsTo(channel.getName()))
            return false; // already exists

        return channels.add(new AnnouncementChannelEntity(this, channel));
    }

    public boolean removeChannel(ChannelEntity channel)
    {
        String channelName = channel.getName();

        AnnouncementChannelEntity r = null;
        for (AnnouncementChannelEntity ac: channels)
            if (ac.getChannel().getName().equalsIgnoreCase(channelName)) {
                r = ac;
                break;
            }

        if (r != null)
            return channels.remove(r);
        return false;
    }

    public boolean addTranslation(Locale locale, String title, String content)
	{
        if (getTranslation(locale) != null)
            return false; // already exists

        return translations.add(
            new AnnouncementTranslationEntity(this, locale, title, content));
	}

    public boolean removeTranslation(Locale locale)
	{
        String langCode = locale.getLanguage();

        AnnouncementTranslationEntity r = null;
        for (AnnouncementTranslationEntity t: translations)
            if (t.getLocale().equals(langCode)) {
                r = t;
                break;
            }

        if (r != null)
            return translations.remove(r);
        return false;
	}

	public AnnouncementTranslationEntity getTranslation(String langCode)
	{
	    for (AnnouncementTranslationEntity t: translations)
	        if (t.getLocale().equals(langCode))
	            return t;
	    return null;
	}

	public AnnouncementTranslationEntity getTranslation(Locale locale)
	{
	    return getTranslation(locale.getLanguage());
	}
}
