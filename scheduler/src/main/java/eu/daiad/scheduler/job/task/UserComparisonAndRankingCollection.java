package eu.daiad.scheduler.job.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

/**
 * Stores data about comparisons and rankings for a group of users.
 */
class UserComparisonAndRankingCollection {

    /**
     * A collection of user comparison and ranking data.
     */
    private Map<UUID, UserComparisonAndRanking> users = new HashMap<UUID, UserComparisonAndRanking>();

    /**
     * Creates a new instance of {@link UserComparisonAndRankingCollection}.
     */
    public UserComparisonAndRankingCollection() {
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
        if (user1.getKey().equals(user2.getKey())) {
            return true;
        }
        if ((user1.getNeighborhoodId() != null) && (user2.getNeighborhoodId() != null)) {
            return user1.getNeighborhoodId().equals(user2.getNeighborhoodId());
        }
        return false;
    }

    /**
     * Adds a user to the collection.
     *
     * @param key the unique user key.
     * @param householdSize the household size.
     * @param volume the water consumption of the user for the last month.
     * @param location the location of the user as inferred by the registered smart water meter.
     * @param neighborhoodId the neighborhood id.
     * @param serial water meter serial number.
     */
    public void addUser(UUID key, int householdSize, double volume, Geometry location, Integer neighborhoodId, String serial) {
        // Create user
        UserComparisonAndRanking user = new UserComparisonAndRanking(key, householdSize, volume, location, neighborhoodId, serial);
        users.put(key, user);

        // Update similar/neighbor users for all users
        for (UserComparisonAndRanking u : users.values()) {
            if (isSimilar(user, u)) {
                u.addSimilar(user);
            }

            if (isNeighbor(user, u)) {
                u.addNeighbor(user);
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
            volume += (user.getSelf().volume * user.getHouseholdSize());
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / getAllTotalMembers();
        result.value = Math.round((float) value / users.size());

        return result;
    }

    /**
     * Returns the total number of household members.
     *
     * @return the total number of household members.
     */
    public int getAllTotalMembers() {
        int total = 0;
        for (UserComparisonAndRanking user : users.values()) {
            total += user.getHouseholdSize();
        }
        return total;
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
