package eu.daiad.web.job.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Stores data about comparisons and rankings for a single user.
 */
class UserComparisonAndRanking {

    /**
     * The user unique key.
     */
    private UUID key;

    /**
     * Household size.
     */
    private int householdSize;

    /**
     * User location as inferred by the assigned smart water meter.
     */
    private Geometry location;

    /**
     * User's water IQ data.
     */
    private WaterIq self = new WaterIq();

    /**
     * Comparison and ranking data for users similar to this user.
     */
    private List<UserComparisonAndRanking> similar = new ArrayList<UserComparisonAndRanking>();

    /**
     * Comparison and ranking data for neighbors of this user.
     */
    private List<UserComparisonAndRanking> nearest = new ArrayList<UserComparisonAndRanking>();

    /**
     * Creates a new instance of {@link UserComparisonAndRanking}.
     *
     * @param key the user key.
     * @param householdSize the household size.
     * @param volume the user monthly water consumption.
     * @param location the user location.
     */
    public UserComparisonAndRanking(UUID key, int householdSize, double volume, Geometry location) {
        this.key = key;
        this.householdSize = householdSize;
        this.location = location;

        self.volume = volume;

    }

    public UUID getKey() {
        return key;
    }

    public UUID[] getSimilarUsers() {
        List<UUID> keys = new ArrayList<UUID>();
        for (UserComparisonAndRanking u : similar) {
            keys.add(u.getKey());
        }
        return keys.toArray(new UUID[] {});
    }

    public UUID[] getNearestUsers() {
        List<UUID> keys = new ArrayList<UUID>();
        for (UserComparisonAndRanking u : nearest) {
            keys.add(u.getKey());
        }
        return keys.toArray(new UUID[] {});
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public Geometry getLocation() {
        return location;
    }

    public WaterIq getSelf() {
        return self;
    }

    public void addNearest(UserComparisonAndRanking user) {
        nearest.add(user);
    }

    public void addSimilar(UserComparisonAndRanking user) {
        similar.add(user);
    }

    /**
     * Computes the water IQ of similar users.
     *
     * @return the water IQ of similar users.
     */
    public WaterIq getSimilarWaterIq() {
        double volume = 0;
        int value = 0;

        for (UserComparisonAndRanking user : similar) {
            volume += user.getSelf().volume;
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / similar.size();
        result.value = Math.round((float) value / similar.size());

        return result;
    }

    /**
     * Computes the water IQ of neighbors.
     *
     * @return the water IQ of neighbors.
     */
    public WaterIq getNearestWaterIq() {
        double volume = 0;
        int value = 0;

        for (UserComparisonAndRanking user : nearest) {
            volume += user.getSelf().volume;
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / nearest.size();
        result.value = Math.round((float) value / nearest.size());

        return result;
    }
}
