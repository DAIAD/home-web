package eu.daiad.web.service.etl.model;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

/**
 * Represents a phase in trial.
 */
public class Phase {

    public enum EnumSessionSelectionMode {
        UNDEFINED, NEAREST, INTERPOLATION, MIN, MAX;
    }

    private EnumPhase phase;

    private long startTimestamp;

    private long endTimestamp;

    private Long minSessionId;

    private Long maxSessionId;

    private Phase previous = null;

    private Phase next = null;

    private EnumSessionSelectionMode LeftBoundMode;

    private EnumSessionSelectionMode RightBoundMode;

    public static Phase Empty = new Phase();

    private Phase() {
        phase = EnumPhase.EMPTY;
        startTimestamp = endTimestamp = DateTime.now().getMillis();
    }

    public Phase(EnumPhase phase, long startTimestamp) {
        if (phase == EnumPhase.EMPTY) {
            throw new RuntimeException("Cannot create empty phase.");
        }

        this.phase = phase;
        this.startTimestamp = startTimestamp;
        endTimestamp = DateTime.now().getMillis();
    }

    public void invalidate() {
        phase = EnumPhase.EMPTY;
        previous = next = null;
    }

    public void setAdjacent(Phase previous, Phase next) {
        this.previous = previous;
        this.next = next;

        if ((previous != null) && (previous.getPhase() == EnumPhase.EMPTY)) {
            throw new RuntimeException("Cannot set previous adjacent phase to an empty phase.");
        }
        if ((next != null) && (next.getPhase() == EnumPhase.EMPTY)) {
            throw new RuntimeException("Cannot set next adjacent phase to an empty phase.");
        }
        if (next != null) {
            endTimestamp = next.getStartTimestamp();
        }

        setSelectionModes();
    }

    public EnumPhase getPhase() {
        return phase;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public Long getMinSessionId() {
        return minSessionId;
    }

    public void setMinSessionId(Long minSessionId) {
        this.minSessionId = minSessionId;
    }

    public Long getMaxSessionId() {
        return maxSessionId;
    }

    public void setMaxSessionId(Long maxSessionId) {
        this.maxSessionId = maxSessionId;
    }

    public boolean isSessionSet() {
        return ((minSessionId != null) && (maxSessionId != null));
    }
    public int getDays() {
        return Days.daysBetween(new DateTime(startTimestamp, DateTimeZone.UTC), new DateTime(endTimestamp, DateTimeZone.UTC)).getDays();
    }

    public Phase getPrevious() {
        return previous;
    }

    public Phase getNext() {
        return next;
    }

    public EnumSessionSelectionMode getLeftBoundSelection() {
        return LeftBoundMode;
    }

    public EnumSessionSelectionMode getRightBoundSelection() {
        return RightBoundMode;
    }

    private void setSelectionModes() {
        if (previous == null) {
            LeftBoundMode = EnumSessionSelectionMode.MIN;
        } else {
            LeftBoundMode = resolveSelectionMode(previous.getPhase(), phase);
        }
        if (next == null) {
            RightBoundMode = EnumSessionSelectionMode.MAX;
        } else {
            RightBoundMode = resolveSelectionMode(phase, next.getPhase());
        }
    }

    private EnumSessionSelectionMode resolveSelectionMode(EnumPhase prev, EnumPhase next) {
        EnumSessionSelectionMode mode =  EnumSessionSelectionMode.UNDEFINED;

        if (Modes.containsKey(next)) {
            mode = Modes.get(next);
        }

        if(mode == EnumSessionSelectionMode.UNDEFINED) {
            throw new RuntimeException(String.format("Cannot resolve session selection mode for phases [%s] and [%s].",
                                                     prev.toString(), next.toString()));
        }

        return mode;
    }

    private static final Map<EnumPhase, EnumSessionSelectionMode> Modes;
    static
    {
        Modes = new HashMap<EnumPhase, EnumSessionSelectionMode>();

        Modes.put(EnumPhase.BASELINE, EnumSessionSelectionMode.UNDEFINED);
        Modes.put(EnumPhase.AMPHIRO_ON, EnumSessionSelectionMode.NEAREST);
        Modes.put(EnumPhase.AMPHIRO_ON_MOBILE_ON, EnumSessionSelectionMode.INTERPOLATION);
        Modes.put(EnumPhase.AMPHIRO_ON_MOBILE_ON_SOCIAL_ON, EnumSessionSelectionMode.INTERPOLATION);
        Modes.put(EnumPhase.MOBILE_ON, EnumSessionSelectionMode.INTERPOLATION);
        Modes.put(EnumPhase.MOBILE_ON_AMPHIRO_ON, EnumSessionSelectionMode.NEAREST);
        Modes.put(EnumPhase.MOBILE_ON_AMPHIRO_ON_SOCIAL_ON, EnumSessionSelectionMode.INTERPOLATION);
        Modes.put(EnumPhase.MOBILE_ON_SOCIAL_ON, EnumSessionSelectionMode.INTERPOLATION);
        Modes.put(EnumPhase.MOBILE_ON_SOCIAL_ON_AMPHIRO_ON, EnumSessionSelectionMode.NEAREST);
    }
}
