package eu.daiad.web.model.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents water IQ data for a single user.
 */
public class ComparisonRanking {

    public WaterIqCollection waterIq = new WaterIqCollection();

    public MonthlyConsumtpion last1MonthConsumption = new MonthlyConsumtpion();

    public MonthlyConsumtpion last6MonthConsumption = new MonthlyConsumtpion();

    public static class WaterIq {

        public double volume;

        public String value;

    }

    public static class WaterIqWithTimestamp extends WaterIq {

        public long timestamp;

        public String from;

        public String to;

    }

    public static class WaterIqCollection {

        public List<WaterIq> user = new ArrayList<WaterIq>();

        public WaterIq similar = new WaterIq();

        public WaterIq nearest = new WaterIq();

        public WaterIq all = new WaterIq();
    }

    public static class MonthlyConsumtpion {

        public MonthlyConsumtpion() {

        }

        public MonthlyConsumtpion(Double user, Double similar, Double nearest, Double all) {
            this.user = (user == null ? 0 : user);
            this.similar = (similar == null ? 0 : similar);
            this.nearest = (nearest == null ? 0 : nearest);
            this.all = (all == null ? 0 : all);
        }

        public double user;

        public double similar;

        public double nearest;

        public double all;
    }
}
