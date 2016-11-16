package eu.daiad.web.model.amphiro;

public class AmphiroSessionDeleteAction {

    private long timestamp;

    public AmphiroSessionDeleteAction() {

    }

    public AmphiroSessionDeleteAction(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}