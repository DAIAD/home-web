package eu.daiad.scheduler.service.etl.model;

/**
 * Represents a state transition.
 */
public class Transition {

    private EnumTransition transition;

    private long timestamp;

    public Transition(EnumTransition transition, long timestamp) {
        this.transition = transition;
        this.timestamp = timestamp;
    }

    public EnumTransition getTransition() {
        return transition;
    }

    public long getTimestamp() {
        return timestamp;
    }

}