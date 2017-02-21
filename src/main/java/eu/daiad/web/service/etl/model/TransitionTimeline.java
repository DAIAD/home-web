package eu.daiad.web.service.etl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the timeline of state transitions.
 */
public class TransitionTimeline {

    List<Transition> transitions = new ArrayList<Transition>();

    /**
     * Adds a new transition to the timeline.
     *
     * @param transition the new transition type.
     * @param timestamp the timestamp of the transition.
     * @throws Exception if transition already exists or the timestamp is the same with that of an existing transition.
     */
    public void add(EnumTransition transition, long timestamp) throws RuntimeException {
        add(new Transition(transition, timestamp));
    }

    /**
     * Adds a new transition to the timeline.
     *
     * @param transition the new transition.
     * @throws Exception if transition already exists or the timestamp is the same with that of an existing transition.
     */
    public void add(Transition transition) throws RuntimeException {
        Transition existing = getTransitionByType(transition.getTransition());

        if (existing == null) {
            transitions.add(transition);
        } else if (existing.getTimestamp() > transition.getTimestamp()) {
            // Replace with the least recent record
            transitions.remove(existing);

            transitions.add(transition);
        } else {
            // The least recent record has already bean found
            return;
        }

        Collections.sort(transitions, new Comparator<Transition>() {

            @Override
            public int compare(Transition t1, Transition t2) {
                // Sort first by transition type
                if ((t1.getTransition() == EnumTransition.AMPHIRO_PAIRED) && (t2.getTransition() != EnumTransition.AMPHIRO_PAIRED)) {
                    return -1;
                }
                if ((t1.getTransition() != EnumTransition.AMPHIRO_PAIRED) && (t2.getTransition() == EnumTransition.AMPHIRO_PAIRED)) {
                    return 1;
                }

                if ((t1.getTransition() == EnumTransition.MOBILE_ON) && (t2.getTransition() == EnumTransition.SOCIAL_ON)) {
                    return -1;
                }
                if ((t1.getTransition() == EnumTransition.SOCIAL_ON) && (t2.getTransition() == EnumTransition.MOBILE_ON)) {
                    return 1;
                }

                if (t1.getTimestamp() == t2.getTimestamp()) {
                    throw new RuntimeException("Transition timestamp must be unique.");
                } else if (t1.getTimestamp() < t2.getTimestamp()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    public Iterator<Transition> getIterator() {
        return transitions.iterator();
    }

    public Long getTimestampByType(EnumTransition type) {
        for(Transition t : transitions) {
            if(t.getTransition().equals(type)) {
                return t.getTimestamp();
            }
        }

        return null;
    }

    public Transition getTransitionByType(EnumTransition type) {
        for(Transition t : transitions) {
            if(t.getTransition().equals(type)) {
                return t;
            }
        }

        return null;
    }
}

