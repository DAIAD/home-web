package eu.daiad.web.job.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Stores data about comparisons and rankings for a group of users.
 */
class UserComparisonAndRankingCollection {

    /**
     * Distance used for deciding the nearest neighbors.
     */
    private float distance;

    /**
     * A collection of user comparison and ranking data.
     */
    private Map<UUID, UserComparisonAndRanking> users = new HashMap<UUID, UserComparisonAndRanking>();

    /**
     * Creates a new instance of {@link UserComparisonAndRankingCollection}.
     *
     * @param distance distance used for deciding the nearest neighbors.
     */
    public UserComparisonAndRankingCollection(float distance) {
        this.distance = distance;
    }

    /**
     * Computes the spatial distance in meters for the given geometries.
     *
     * @param g1 the first geometry.
     * @param g2 the second geometry.
     * @return the geometry objects distance in meters.
     */
    private double distance(Geometry g1, Geometry g2) {
        double distance = g1.distance(g2);

        return distance * (Math.PI / 180) * 6378137;
    }

    /**
     * Determines if the given users are similar.
     *
     * @param user1 the fist user.
     * @param user2 the second user.
     * @return true if the users are similar.
     */
    private boolean isSimilar(UserComparisonAndRanking user1, UserComparisonAndRanking user2) {
        return (user1.getHouseholdSize() == user2.getHouseholdSize());
    }

    /**
     * Determines if the given users are neighbors.
     *
     * @param user1 the fist user.
     * @param user2 the second user.
     * @return true if the users are neighbors.
     */
    private boolean isNeighbor(UserComparisonAndRanking user1, UserComparisonAndRanking user2) {
        return ((user1.getLocation() != null) &&
                (user2.getLocation() != null) &&
                (distance(user1.getLocation(), user2.getLocation()) <= distance));
    }

    /**
     * Adds a user to the collection.
     *
     * @param key the unique user key.
     * @param householdSize the household size.
     * @param volume the water consumption of the user for the last month.
     * @param location the location of the user as inferred by the registered smart water meter.
     */
    public void addUser(UUID key, int householdSize, double volume, Geometry location) {
        // Create user
        UserComparisonAndRanking user = new UserComparisonAndRanking(key, householdSize, volume, location);
        users.put(key, user);

        // Update similar/nearest users for all users
        for (UserComparisonAndRanking u : users.values()) {
            if (isSimilar(user, u)) {
                u.addSimilar(user);
            }

            if (isNeighbor(user, u)) {
                u.addNearest(user);
            }
        }
    }

    /**
     * Computes the water IQ for all the users in the group.
     *
     * @return the computed water IQ properties.
     */
    public WaterIq getAll() {
        double volume = 0;
        int value = 0;

        for (UserComparisonAndRanking user : users.values()) {
            volume += user.getSelf().volume;
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / users.size();
        result.value = Math.round((float) value / users.size());

        return result;
    }

    /**
     * Returns a set of all user keys.
     *
     * @return a set of user keys.
     */
    public Set<UUID> getUserKeys() {
        return users.keySet();
    }

    /**
     * Gets the user with the given key.
     *
     * @param key the user key.
     * @return the user.
     */
    public UserComparisonAndRanking getUserByKey(UUID key) {
        return users.get(key);
    }

}
