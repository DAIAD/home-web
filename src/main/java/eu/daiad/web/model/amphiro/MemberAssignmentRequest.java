package eu.daiad.web.model.amphiro;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.AuthenticatedRequest;

public class MemberAssignmentRequest extends AuthenticatedRequest {

    private List<Assignment> assignments = new ArrayList<Assignment>();

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public static class Assignment {

        private UUID deviceKey;

        private long sessionId;

        private int memberIndex;

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

        public int getMemberIndex() {
            return memberIndex;
        }

        public void setMemberIndex(int memberIndex) {
            this.memberIndex = memberIndex;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

    }
}
