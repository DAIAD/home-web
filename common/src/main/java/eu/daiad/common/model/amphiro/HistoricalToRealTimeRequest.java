package eu.daiad.common.model.amphiro;

import java.util.UUID;

import eu.daiad.common.model.AuthenticatedRequest;

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
