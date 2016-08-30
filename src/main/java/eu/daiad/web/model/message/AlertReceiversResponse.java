package eu.daiad.web.model.message;
import eu.daiad.web.model.RestResponse;
import java.util.List;

public class AlertReceiversResponse extends RestResponse{
    
    private List<ReceiverAccount> receivers;
    private Alert alert;

    public List<ReceiverAccount> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<ReceiverAccount> receivers) {
        this.receivers = receivers;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

}
