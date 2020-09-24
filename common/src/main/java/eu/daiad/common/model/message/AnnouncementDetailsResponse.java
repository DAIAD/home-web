package eu.daiad.common.model.message;

import java.util.List;

import eu.daiad.common.model.RestResponse;

public class AnnouncementDetailsResponse extends RestResponse{
    
    private List<ReceiverAccount> receivers;
    private Announcement announcement;

    public List<ReceiverAccount> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<ReceiverAccount> receivers) {
        this.receivers = receivers;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }
}
