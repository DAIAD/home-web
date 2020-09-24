package eu.daiad.common.model.amphiro;

/**
 * Helper class for storing the historical / real-time versions of an
 * amphiro b1 session.
 */
public class SessionVersions {

    public final AmphiroSessionDetails historical;

    public final AmphiroSessionDetails realtime;

    public SessionVersions(AmphiroSessionDetails historical, AmphiroSessionDetails realtime) {
        this.historical = historical;
        this.realtime = realtime;
    }

    public boolean isEmpty() {
        return ((historical == null) && (realtime == null));
    }

    public Long getSessionId() {
        if (realtime != null) {
            return realtime.getId();
        }
        if (historical != null) {
            return historical.getId();
        }
        return null;
    }

}
