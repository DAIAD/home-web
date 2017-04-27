package eu.daiad.web.model.message;

import java.util.List;

import eu.daiad.web.model.AuthenticatedRequest;

public class AnnouncementRequest extends AuthenticatedRequest
{
    private Announcement announcement;

    private List<ReceiverAccount> receivers;

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }

    public List<ReceiverAccount> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<ReceiverAccount> receivers) {
        this.receivers = receivers;
    }

}
