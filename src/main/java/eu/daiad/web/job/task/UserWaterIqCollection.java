package eu.daiad.web.job.task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Store for managing water IQ data for multiple users.
 */
class UserWaterIqCollection {

    private float distance;

    private UUID utilityKey;

    private Map<UUID, UserWaterIq> users = new HashMap<UUID, UserWaterIq>();

    public UserWaterIqCollection(UUID utilityKey, float distance) {
        this.distance = distance;
        this.utilityKey = utilityKey;
    }

    public Map<UUID, UserWaterIq> getUsers() {
        return users;
    }

    public UUID getUtilityKey() {
        return utilityKey;
    }

    public void addUser(UUID key, int householdSize, double volume, Geometry location) {
        // Create user
        UserWaterIq user = new UserWaterIq(key, householdSize, volume, location);
        users.put(key, user);

        // Update similar/nearest users for all users
        for (UserWaterIq u : users.values()) {
            if (u.getHouseholdSize() == householdSize) {
                u.addSimilar(user);
            }

            if ((u.getLocation() != null) &&
                (user.getLocation() != null) &&
                (distance(u.getLocation(), user.getLocation()) <= distance)) {
                u.addNearest(user);
            }
        }
    }

    private double distance(Geometry g1, Geometry g2) {
        double distance = g1.distance(g2);

        return distance * (Math.PI / 180) * 6378137;
    }


    public WaterIq getAll() {
        double volume = 0;
        int value = 0;

        for (UserWaterIq user : users.values()) {
            volume += user.getSelf().volume;
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / users.size();
        result.value = Math.round((float) value / users.size());

        return result;
    }

}
