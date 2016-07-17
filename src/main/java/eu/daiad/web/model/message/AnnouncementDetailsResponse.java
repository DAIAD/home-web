package eu.daiad.web.model.message;

import eu.daiad.web.model.RestResponse;
import java.util.List;

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
