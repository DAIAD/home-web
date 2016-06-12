package eu.daiad.web.model.message;

import eu.daiad.web.model.AuthenticatedRequest;
import java.util.List;

public class AnnouncementRequest extends AuthenticatedRequest{

    private Announcement announcement;

    private List<ReceiverAccount> receiverAccountList;

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }

    public List<ReceiverAccount> getReceiverAccountList() {
        return receiverAccountList;
    }

    public void setReceiverAccountList(List<ReceiverAccount> receiverAccountList) {
        this.receiverAccountList = receiverAccountList;
    }

}
