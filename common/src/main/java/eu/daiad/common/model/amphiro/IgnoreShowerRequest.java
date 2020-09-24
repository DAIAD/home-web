package eu.daiad.common.model.amphiro;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.daiad.common.model.AuthenticatedRequest;

public class IgnoreShowerRequest extends AuthenticatedRequest {

    private List<Session> sessions = new ArrayList<Session>();

    public List<Session> getSessions() {
        return sessions;
    }

    public static class Session {

        private UUID deviceKey;

        private long sessionId;

        private Long timestamp;

        public UUID getDeviceKey() {
            return deviceKey;
        }

        public void setDeviceKey(UUID deviceKey) {
            this.deviceKey = deviceKey;
        }

        public long getSessionId() {
            return sessionId;
        }

        public void setSessionId(long sessionId) {
            this.sessionId = sessionId;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

    }
}
