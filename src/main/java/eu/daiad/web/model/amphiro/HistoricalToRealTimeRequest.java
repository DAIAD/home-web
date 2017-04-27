package eu.daiad.web.model.amphiro;

import java.util.UUID;

import eu.daiad.web.model.AuthenticatedRequest;

public class HistoricalToRealTimeRequest extends AuthenticatedRequest {

    private UUID deviceKey;

    private long sessionId;

    private Long timestamp;

    public UUID getDeviceKey() {
        return deviceKey;
    }

    public long getSessionId() {
        return sessionId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

}
