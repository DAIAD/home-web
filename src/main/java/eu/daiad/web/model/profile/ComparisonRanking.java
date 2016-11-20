package eu.daiad.web.model.profile;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents water IQ data for a single user.
 */
public class ComparisonRanking {

    public WaterIqCollection waterIq = new WaterIqCollection();

    public List<MonthlyConsumtpion> monthlyConsumtpion = new ArrayList<MonthlyConsumtpion>();

    public List<DailyConsumption> dailyConsumtpion = new ArrayList<DailyConsumption>();

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

        public List<WaterIqWithTimestamp> user = new ArrayList<WaterIqWithTimestamp>();

        public WaterIq similar = new WaterIq();

        public WaterIq nearest = new WaterIq();

        public WaterIq all = new WaterIq();
    }

    public static class MonthlyConsumtpion {

        public MonthlyConsumtpion(int month) {
            this.month = month;
        }

        public MonthlyConsumtpion(Double user, Double similar, Double nearest, Double all) {
            this.user = (user == null ? 0 : user);
            this.similar = (similar == null ? 0 : similar);
            this.nearest = (nearest == null ? 0 : nearest);
            this.all = (all == null ? 0 : all);
        }

        public int month;

        public String from;

        public String to;

        public double user;

        public double similar;

        public double nearest;

        public double all;
    }

    public static class DailyConsumption {

        public DailyConsumption(int year, int month, int week, int day) {
            this.year = year;
            this.month = month;
            this.week = week;
            this.day = day;
        }

        public DailyConsumption(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public DailyConsumption(int year, int month, int week, int day, Double user, Double similar, Double nearest, Double all) {
            this.year = year;
            this.month = month;
            this.week = week;
            this.day = day;
            this.user = (user == null ? 0 : user);
            this.similar = (similar == null ? 0 : similar);
            this.nearest = (nearest == null ? 0 : nearest);
            this.all = (all == null ? 0 : all);
        }

        @JsonIgnore
        public int year;

        @JsonIgnore
        public int month;

        public String getDate() {
            return String.format("%d%02d%02d", year, month, day);
        }

        public int week;

        public int day;

        public double user;

        public double similar;

        public double nearest;

        public double all;
    }
}
