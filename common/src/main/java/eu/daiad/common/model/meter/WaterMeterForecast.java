package eu.daiad.common.model.meter;

public class WaterMeterForecast {

    private long timestamp;

    private Float difference;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Float getDifference() {
        return difference;
    }

    public void setDifference(Float difference) {
        this.difference = difference;
    }

}
