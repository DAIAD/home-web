package eu.daiad.web.model.message;

import org.joda.time.DateTime;

public class ReceiverAccount
{
    private int accountId = -1;

    private String username;

    /**
     * This datestamp is meaningful only when this receiver is related with
     * a particular message.
     */
    private DateTime acknowledgedOn;

    public ReceiverAccount() {}

    public ReceiverAccount(int accountId, String username)
    {
        this.accountId = accountId;
        this.username = username;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DateTime getAcknowledgedOn() {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(DateTime acknowledgedOn) {
        this.acknowledgedOn = acknowledgedOn;
    }

}
