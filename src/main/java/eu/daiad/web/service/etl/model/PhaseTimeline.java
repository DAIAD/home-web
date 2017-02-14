package eu.daiad.web.service.etl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the timeline of trial phases.
 */
public class PhaseTimeline {

    private List<Phase> phases = new ArrayList<Phase>();

    /**
     * Adds an empty phase to the timeline.
     */
    public void add() throws RuntimeException {
        this.add(Phase.Empty);
    }

    /**
     * Adds a new phase to the timeline.
     *
     * @param phase phase type.
     * @param startTimestamp start timestamp.
     * @throws RuntimeException if phase already exists or the timestamp is the same with that of an existing phase.
     */
    public void add(EnumPhase phase, long startTimestamp) throws RuntimeException {
        this.add(new Phase(phase, startTimestamp));
    }

    /**
     * Adds a new phase to the timeline.
     *
     * @param phase the new phase.
     * @throws RuntimeException if phase already exists or the timestamp is the same with that of an existing phase.
     */
    public void add(Phase phase) throws RuntimeException {
        if (phase.getPhase() != EnumPhase.EMPTY) {
            for (Phase p : phases) {
                if (p.getPhase().equals(phase.getPhase())) {
                    throw new RuntimeException(String.format("Phase [%s] is not unique in timeline.", phase.getPhase().toString()));
                }
            }
        }

        phases.add(phase);

        alignAndCleanPhases();
    }

    /**
     * Updates sessions end timestamp and removes any overlapping ones.
     */
    private void alignAndCleanPhases() {
        int size = phases.size();

        // Fix end timestamp using the next non-empty phase
        for (int i = 0; i < size; i++) {
            Phase current = phases.get(i);

            if (current.getPhase() != EnumPhase.EMPTY) {
                Phase prev = null;
                for (int j = i - 1; j >= 0; j--) {
                    if (phases.get(j).getPhase() != EnumPhase.EMPTY) {
                        prev = phases.get(j);
                        break;
                    }
                }
                Phase next = null;
                for (int j = i + 1; j < size; j++) {
                    if (phases.get(j).getPhase() != EnumPhase.EMPTY) {
                        next = phases.get(j);
                        break;
                    }
                }
                phases.get(i).setAdjacent(prev, next);
            }
        }
        // Replace overlapping phases with an empty phase
        List<Integer> evicted = new ArrayList<Integer>();
        for (int i = 0; i < size - 1; i++) {
            if (phases.get(i).getPhase() == EnumPhase.EMPTY) {
                continue;
            }
            for (int j = i + 1; j < size; j++) {
                if (phases.get(j).getPhase() == EnumPhase.EMPTY) {
                    continue;
                }
                if (phases.get(i).getStartTimestamp() > phases.get(j).getStartTimestamp()) {
                    evicted.add(i);
                    break;
                }
            }
        }

        for (Integer index : evicted) {
            phases.set(index, Phase.Empty);
        }
    }

    /**
     * Validate phases
     */
    public void validate() {
        for (Phase p1 : phases) {
            for (Phase p2 : phases) {
                if ((p1 != p2) &&
                    (p1.isSessionSet()) &&
                    (p2.isSessionSet())) {
                    if((p1.getMaxSessionId() >= p2.getMinSessionId()) && (p1.getMaxSessionId() <= p2.getMaxSessionId())) {
                        throw new RuntimeException(String.format("Phases [%s] and [%s] shower id overlap. %s", p1.getPhase(), p2.getPhase(), this).trim());
                    }
                    if((p1.getMinSessionId() >= p2.getMinSessionId()) && (p1.getMinSessionId() <= p2.getMaxSessionId())) {
                        throw new RuntimeException(String.format("Phases [%s] and [%s] shower id overlap. %s", p1.getPhase(), p2.getPhase(), this).trim());
                    }
                }
            }
        }
        for (Phase p : phases) {
            if ((p.getMinSessionId() == null) && (p.getMaxSessionId() == null)) {
                // Ignore
            } else if (p.isSessionSet()) {
                if(p.getMinSessionId() > p.getMaxSessionId()) {
                    throw new RuntimeException(String.format("Invalid shower id interval [%d, %d] for phase [%s]. %s",
                                                             p.getMinSessionId(),
                                                             p.getMaxSessionId(),
                                                             p.getPhase(),
                                                             this).trim());
                }
            } else {
                throw new RuntimeException(String.format("Failed to derive phase [%s] min/max shower id. %s", p.getPhase(), this));
            }
        }
    }

    public Phase get(int index) {
        if (index >= phases.size()) {
            return Phase.Empty;
        }
        return phases.get(index);
    }

    public int size() {
        return phases.size();
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();

        for (Phase p : phases) {
            if(p.getPhase() != EnumPhase.EMPTY) {
                text.append(String.format("%s [%d - %d] ", p.getPhase(), p.getMinSessionId(), p.getMaxSessionId()));
            }
        }
        return text.toString();
    }

}
