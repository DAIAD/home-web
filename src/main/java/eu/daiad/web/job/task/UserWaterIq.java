package eu.daiad.web.job.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Stores water IQ data for a single user.
 */
class UserWaterIq {

    private UUID key;

    private int householdSize;

    private Geometry location;

    private WaterIq self = new WaterIq();

    private List<UserWaterIq> similar = new ArrayList<UserWaterIq>();

    private List<UserWaterIq> nearest = new ArrayList<UserWaterIq>();

    public UserWaterIq(UUID key, int householdSize, double volume, Geometry location) {
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
        for (UserWaterIq u : similar) {
            keys.add(u.getKey());
        }
        return keys.toArray(new UUID[] {});
    }

    public UUID[] getNearestUsers() {
        List<UUID> keys = new ArrayList<UUID>();
        for (UserWaterIq u : nearest) {
            keys.add(u.getKey());
        }
        return keys.toArray(new UUID[] {});
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(int householdSize) {
        this.householdSize = householdSize;
    }

    public Geometry getLocation() {
        return location;
    }

    public WaterIq getSelf() {
        return self;
    }

    public void addNearest(UserWaterIq user) {
        nearest.add(user);
    }

    public void addSimilar(UserWaterIq user) {
        similar.add(user);
    }

    public WaterIq getSimilarWaterIq() {
        double volume = 0;
        int value = 0;

        for (UserWaterIq user : similar) {
            volume += user.getSelf().volume;
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / similar.size();
        result.value = Math.round((float) value / similar.size());

        return result;
    }

    public WaterIq getNearestWaterIq() {
        double volume = 0;
        int value = 0;

        for (UserWaterIq user : nearest) {
            volume += user.getSelf().volume;
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / nearest.size();
        result.value = Math.round((float) value / nearest.size());

        return result;
    }
}
