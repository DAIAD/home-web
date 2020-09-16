package eu.daiad.web.job.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

/**
 * Stores data about comparisons and rankings for a single user.
 */
class UserComparisonAndRanking {

    /**
     * The user unique key.
     */
    private UUID key;

    /**
     * The user water meter serial number.
     */
    private String serial;

    /**
     * Household size.
     */
    private int householdSize;

    /**
     * User location as inferred by the assigned smart water meter.
     */
    private Geometry location;

    /**
     * User neighborhood id.
     */
    private Integer neighborhoodId;

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
    private List<UserComparisonAndRanking> neighbor = new ArrayList<UserComparisonAndRanking>();

    /**
     * Creates a new instance of {@link UserComparisonAndRanking}.
     *
     * @param key the user key.
     * @param householdSize the household size.
     * @param volume the user monthly water consumption.
     * @param location the user location.
     * @param neighborhoodId the neighborhood id.
     * @param serial water meter serial number.
     */
    public UserComparisonAndRanking(UUID key, int householdSize, double volume, Geometry location, Integer neighborhoodId, String serial) {
        this.key = key;
        this.householdSize = householdSize;
        this.location = location;
        this.neighborhoodId = neighborhoodId;
        this.serial = serial;

        self.volume = volume;
    }

    public UUID getKey() {
        return key;
    }

    public void overrideSimilar(List<UserComparisonAndRanking> users) {
        similar.clear();
        similar.addAll(users);
    }

    public UUID[] getSimilarUsers() {
        List<UUID> keys = new ArrayList<UUID>();
        for (UserComparisonAndRanking u : similar) {
            keys.add(u.getKey());
        }
        return keys.toArray(new UUID[] {});
    }

    public int getSimilarTotalMembers() {
        int total = 0;
        for (UserComparisonAndRanking u : similar) {
            total += u.getHouseholdSize();
        }
        return total;
    }

    public UUID[] getNeighborUsers() {
        List<UUID> keys = new ArrayList<UUID>();
        for (UserComparisonAndRanking u : neighbor) {
            keys.add(u.getKey());
        }
        return keys.toArray(new UUID[] {});
    }

    public int getNeighborTotalMembers() {
        int total = 0;
        for (UserComparisonAndRanking u : neighbor) {
            total += u.getHouseholdSize();
        }
        return total;
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public Geometry getLocation() {
        return location;
    }

    public Integer getNeighborhoodId() {
        return neighborhoodId;
    }

    public String getSerial() {
        return serial;
    }

    public WaterIq getSelf() {
        return self;
    }

    public void addNeighbor(UserComparisonAndRanking user) {
        neighbor.add(user);
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
            volume += (user.getSelf().volume * user.getHouseholdSize());
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / getSimilarTotalMembers();
        result.value = Math.round((float) value / similar.size());

        return result;
    }

    /**
     * Computes the water IQ of neighbors.
     *
     * @return the water IQ of neighbors.
     */
    public WaterIq getNeighborWaterIq() {
        double volume = 0;
        int value = 0;

        for (UserComparisonAndRanking user : neighbor) {
            volume += (user.getSelf().volume * user.getHouseholdSize());
            value += user.getSelf().value;
        }

        WaterIq result = new WaterIq();
        result.volume = volume / getNeighborTotalMembers();
        result.value = Math.round((float) value / neighbor.size());

        return result;
    }
}
