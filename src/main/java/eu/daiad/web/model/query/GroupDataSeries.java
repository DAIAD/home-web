package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.Predicate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import eu.daiad.web.model.EnumTimeAggregation;

public class GroupDataSeries
{
    private String label;

    private Long areaId;

    private int population;

    List<DataPoint> points = new ArrayList<DataPoint>();

    public GroupDataSeries(String label, int population, Long areaId) {
        this.label = label;
        this.population = population;
        this.areaId = areaId;
    }

    public String getLabel() {
        return label;
    }

    public List<DataPoint> getPoints() {
        return points;
    }

    private DataPoint createDataPoint(List<EnumMetric> metrics, DataPoint.EnumDataPointType type, Long timestamp) {
        switch (type) {
            case METER:
                MeterDataPoint mp = (timestamp == null ? new MeterDataPoint() : new MeterDataPoint(timestamp));

                for (EnumMetric m : metrics) {
                    if (m == EnumMetric.MIN) {
                        mp.getVolume().put(m, Double.MAX_VALUE);
                    } else {
                        mp.getVolume().put(m, 0.0);
                    }
                }

                points.add(mp);

                return mp;
            case AMPHIRO:
                AmphiroDataPoint ap = (timestamp == null ? new AmphiroDataPoint() : new AmphiroDataPoint(timestamp));

                for (EnumMetric m : metrics) {
                    if (m == EnumMetric.MIN) {
                        ap.getVolume().put(m, Double.MAX_VALUE);
                        ap.getEnergy().put(m, Double.MAX_VALUE);
                        ap.getTemperature().put(m, Double.MAX_VALUE);
                        ap.getDuration().put(m, Double.MAX_VALUE);
                        ap.getFlow().put(m, Double.MAX_VALUE);
                    } else {
                        ap.getVolume().put(m, 0.0);
                        ap.getEnergy().put(m, 0.0);
                        ap.getTemperature().put(m, 0.0);
                        ap.getDuration().put(m, 0.0);
                        ap.getFlow().put(m, 0.0);
                    }
                }

                points.add(ap);

                return ap;
            default:
                throw new IllegalArgumentException("Data point type is not supported.");

        }
    }

    private DataPoint getDataPoint(EnumTimeAggregation granularity,
                                   long timestamp,
                                   List<EnumMetric> metrics,
                                   DataPoint.EnumDataPointType type,
                                   DateTimeZone timezone) {
        DateTime date = new DateTime(timestamp, timezone);

        switch (granularity) {
            case HOUR:
                date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), 0, 0, timezone);
                break;
            case DAY:
                date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0, timezone);
                break;
            case WEEK:
                DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

                date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 0, 0, 0, timezone);
                break;
            case MONTH:
                date = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0, 0, timezone);
                break;
            case YEAR:
                date = new DateTime(date.getYear(), 1, 1, 0, 0, 0, timezone);
                break;
            default:
                throw new IllegalArgumentException("Granularity level not supported.");
        }

        DataPoint p = null;
        timestamp = date.getMillis();

        for (int i = 0, count = points.size(); i < count; i++) {
            if (timestamp == points.get(i).getTimestamp()) {
                p = points.get(i);

                break;
            }
        }

        if (p == null) {
            p = createDataPoint(metrics, type, timestamp);
        }

        return p;
    }

    public void addAmhiroDataPoint(EnumTimeAggregation granularity,
                                   long timestamp,
                                   double volume,
                                   double energy,
                                   double duration,
                                   double temperature,
                                   double flow,
                                   List<EnumMetric> metrics,
                                   DateTimeZone timezone) {
        AmphiroDataPoint point = (AmphiroDataPoint) getDataPoint(granularity, timestamp, metrics, DataPoint.EnumDataPointType.AMPHIRO, timezone);

        boolean avg = false;
        for (EnumMetric m : metrics) {
            switch (m) {
                case COUNT:
                    point.getVolume().put(m, point.getVolume().get(m) + 1);
                    point.getEnergy().put(m, point.getEnergy().get(m) + 1);
                    point.getDuration().put(m, point.getDuration().get(m) + 1);
                    point.getTemperature().put(m, point.getTemperature().get(m) + 1);
                    point.getFlow().put(m, point.getFlow().get(m) + 1);
                    break;
                case SUM:
                    point.getVolume().put(m, point.getVolume().get(m) + volume);
                    point.getEnergy().put(m, point.getEnergy().get(m) + energy);
                    point.getDuration().put(m, point.getDuration().get(m) + duration);
                    point.getTemperature().put(m, point.getTemperature().get(m) + temperature);
                    point.getFlow().put(m, point.getFlow().get(m) + flow);
                    break;
                case MIN:
                    if (point.getVolume().get(m) > volume) {
                        point.getVolume().put(m, volume);
                    }
                    if (point.getEnergy().get(m) > energy) {
                        point.getEnergy().put(m, energy);
                    }
                    if (point.getDuration().get(m) > duration) {
                        point.getDuration().put(m, duration);
                    }
                    if (point.getTemperature().get(m) > temperature) {
                        point.getTemperature().put(m, temperature);
                    }
                    if (point.getFlow().get(m) > flow) {
                        point.getFlow().put(m, flow);
                    }
                    break;
                case MAX:
                    if (point.getVolume().get(m) < volume) {
                        point.getVolume().put(m, volume);
                    }
                    if (point.getEnergy().get(m) < energy) {
                        point.getEnergy().put(m, energy);
                    }
                    if (point.getDuration().get(m) < duration) {
                        point.getDuration().put(m, duration);
                    }
                    if (point.getTemperature().get(m) < temperature) {
                        point.getTemperature().put(m, temperature);
                    }
                    if (point.getFlow().get(m) < flow) {
                        point.getFlow().put(m, flow);
                    }
                    break;
                case AVERAGE:
                    avg = true;
                default:
                    // Ignore
            }
        }
        if (avg) {
            double count = point.getVolume().get(EnumMetric.COUNT);
            if (count == 0) {
                point.getVolume().put(EnumMetric.AVERAGE, 0.0);
                point.getEnergy().put(EnumMetric.AVERAGE, 0.0);
                point.getDuration().put(EnumMetric.AVERAGE, 0.0);
                point.getTemperature().put(EnumMetric.AVERAGE, 0.0);
                point.getFlow().put(EnumMetric.AVERAGE, 0.0);
            } else {
                point.getVolume().put(EnumMetric.AVERAGE, point.getVolume().get(EnumMetric.SUM) / count);
                point.getEnergy().put(EnumMetric.AVERAGE, point.getEnergy().get(EnumMetric.SUM) / count);
                point.getDuration().put(EnumMetric.AVERAGE, point.getDuration().get(EnumMetric.SUM) / count);
                point.getTemperature().put(EnumMetric.AVERAGE, point.getTemperature().get(EnumMetric.SUM) / count);
                point.getFlow().put(EnumMetric.AVERAGE, point.getFlow().get(EnumMetric.SUM) / count);
            }
        }
    }

    public void addMeterRankingDataPoint(EnumTimeAggregation granularity,
                                         UUID key,
                                         String label,
                                         long timestamp,
                                         double difference,
                                         double volume,
                                         List<EnumMetric> metrics,
                                         DateTimeZone timezone) {
        MeterUserDataPoint point = (MeterUserDataPoint) getUserDataPoint(granularity,
                                                                         key,
                                                                         label,
                                                                         timestamp,
                                                                         metrics,
                                                                         DataPoint.EnumDataPointType.METER,
                                                                         timezone);

        for (EnumMetric m : metrics) {
            switch (m) {
                case COUNT:
                    point.getVolume().put(m, point.getVolume().get(m) + 1);
                    break;
                case SUM:
                    point.getVolume().put(m, point.getVolume().get(m) + difference);
                    break;
                case MIN:
                    if (point.getVolume().get(m) > (volume - difference)) {
                        point.getVolume().put(m, (volume - difference));
                    }
                    break;
                case MAX:
                    if (point.getVolume().get(m) < volume) {
                        point.getVolume().put(m, volume);
                    }
                    break;
                default:
                    // Ignore
            }
        }
    }

    public void addAmphiroRankingDataPoint(EnumTimeAggregation granularity,
                                           UUID key,
                                           String label,
                                           long timestamp,
                                           double volume,
                                           double energy,
                                           double duration,
                                           double temperature,
                                           double flow,
                                           List<EnumMetric> metrics,
                                           DateTimeZone timezone) {
        AmphiroUserDataPoint point = (AmphiroUserDataPoint) getUserDataPoint(granularity,
                                                                             key,
                                                                             label,
                                                                             timestamp,
                                                                             metrics,
                                                                             DataPoint.EnumDataPointType.AMPHIRO,
                                                                             timezone);

        boolean avg = false;
        for (EnumMetric m : metrics) {
            switch (m) {
                case COUNT:
                    point.getVolume().put(m, point.getVolume().get(m) + 1);
                    point.getEnergy().put(m, point.getEnergy().get(m) + 1);
                    point.getDuration().put(m, point.getDuration().get(m) + 1);
                    point.getTemperature().put(m, point.getTemperature().get(m) + 1);
                    point.getFlow().put(m, point.getFlow().get(m) + 1);
                    break;
                case SUM:
                    point.getVolume().put(m, point.getVolume().get(m) + volume);
                    point.getEnergy().put(m, point.getEnergy().get(m) + energy);
                    point.getDuration().put(m, point.getDuration().get(m) + duration);
                    point.getTemperature().put(m, point.getTemperature().get(m) + temperature);
                    point.getFlow().put(m, point.getFlow().get(m) + flow);
                    break;
                case MIN:
                    if (point.getVolume().get(m) > volume) {
                        point.getVolume().put(m, volume);
                    }
                    if (point.getEnergy().get(m) > energy) {
                        point.getEnergy().put(m, energy);
                    }
                    if (point.getDuration().get(m) > duration) {
                        point.getDuration().put(m, duration);
                    }
                    if (point.getTemperature().get(m) > temperature) {
                        point.getTemperature().put(m, temperature);
                    }
                    if (point.getFlow().get(m) > flow) {
                        point.getFlow().put(m, flow);
                    }
                    break;
                case MAX:
                    if (point.getVolume().get(m) < volume) {
                        point.getVolume().put(m, volume);
                    }
                    if (point.getEnergy().get(m) < energy) {
                        point.getEnergy().put(m, energy);
                    }
                    if (point.getDuration().get(m) < duration) {
                        point.getDuration().put(m, duration);
                    }
                    if (point.getTemperature().get(m) < temperature) {
                        point.getTemperature().put(m, temperature);
                    }
                    if (point.getFlow().get(m) < flow) {
                        point.getFlow().put(m, flow);
                    }
                    break;
                case AVERAGE:
                    avg = true;
                default:
                    // Ignore
            }
        }
        if (avg) {
            double count = point.getVolume().get(EnumMetric.COUNT);
            if (count == 0) {
                point.getVolume().put(EnumMetric.AVERAGE, 0.0);
                point.getEnergy().put(EnumMetric.AVERAGE, 0.0);
                point.getDuration().put(EnumMetric.AVERAGE, 0.0);
                point.getTemperature().put(EnumMetric.AVERAGE, 0.0);
                point.getFlow().put(EnumMetric.AVERAGE, 0.0);
            } else {
                point.getVolume().put(EnumMetric.AVERAGE, point.getVolume().get(EnumMetric.SUM) / count);
                point.getEnergy().put(EnumMetric.AVERAGE, point.getEnergy().get(EnumMetric.SUM) / count);
                point.getDuration().put(EnumMetric.AVERAGE, point.getDuration().get(EnumMetric.SUM) / count);
                point.getTemperature().put(EnumMetric.AVERAGE, point.getTemperature().get(EnumMetric.SUM) / count);
                point.getFlow().put(EnumMetric.AVERAGE, point.getFlow().get(EnumMetric.SUM) / count);
            }
        }
    }

    private RankingDataPoint getRankingDataPoint(EnumTimeAggregation granularity, long timestamp, DateTimeZone timezone) {
        DateTime date = new DateTime(timestamp, timezone);

        switch (granularity) {
            case HOUR:
                date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), 0, 0, timezone);
                break;
            case DAY:
                date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0, timezone);
                break;
            case WEEK:
                DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

                date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 0, 0, 0, timezone);
                break;
            case MONTH:
                date = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0, 0, timezone);
                break;
            case YEAR:
                date = new DateTime(date.getYear(), 1, 1, 0, 0, 0, timezone);
                break;
            default:
                throw new IllegalArgumentException("Granularity level not supported.");
        }

        timestamp = date.getMillis();

        for (int i = 0, count = points.size(); i < count; i++) {
            if (timestamp == points.get(i).getTimestamp()) {
                return (RankingDataPoint) points.get(i);
            }
        }

        RankingDataPoint point = new RankingDataPoint(timestamp);
        points.add(point);

        return point;

    }

    private UserDataPoint getUserDataPoint(EnumTimeAggregation granularity,
                                           UUID key,
                                           String label,
                                           long timestamp,
                                           List<EnumMetric> metrics,
                                           DataPoint.EnumDataPointType type,
                                           DateTimeZone timezone) {
        RankingDataPoint ranking = getRankingDataPoint(granularity, timestamp, timezone);

        for (UserDataPoint point : ranking.getUsers()) {
            if (point.getKey().equals(key)) {
                return point;
            }
        }

        switch (type) {
            case METER:
                MeterUserDataPoint meterPoint = new MeterUserDataPoint(key, label);
                ranking.getUsers().add(meterPoint);

                for (EnumMetric m : metrics) {
                    if (m == EnumMetric.MIN) {
                        meterPoint.getVolume().put(m, Double.MAX_VALUE);
                    } else {
                        meterPoint.getVolume().put(m, 0.0);
                    }

                }

                return meterPoint;
            case AMPHIRO:
                AmphiroUserDataPoint amphiroPoint = new AmphiroUserDataPoint(key, label);
                ranking.getUsers().add(amphiroPoint);

                for (EnumMetric m : metrics) {
                    if (m == EnumMetric.MIN) {
                        amphiroPoint.getVolume().put(m, Double.MAX_VALUE);
                        amphiroPoint.getEnergy().put(m, Double.MAX_VALUE);
                        amphiroPoint.getTemperature().put(m, Double.MAX_VALUE);
                        amphiroPoint.getDuration().put(m, Double.MAX_VALUE);
                        amphiroPoint.getFlow().put(m, Double.MAX_VALUE);
                    } else {
                        amphiroPoint.getVolume().put(m, 0.0);
                        amphiroPoint.getEnergy().put(m, 0.0);
                        amphiroPoint.getTemperature().put(m, 0.0);
                        amphiroPoint.getDuration().put(m, 0.0);
                        amphiroPoint.getFlow().put(m, 0.0);
                    }
                }

                return amphiroPoint;
            default:
                throw new IllegalArgumentException("Data point type is not supported.");
        }
    }

    public int getPopulation() {
        return population;
    }

    public void setPoints(ArrayList<DataPoint> points) {
        this.points = points;
    }

    public Long getAreaId() {
        return areaId;
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public DataPoint getTemporalNearest(long timestamp) {
        DataPoint result = null;
        for (DataPoint p : points) {
            if (result == null) {
                result = p;
            } else if (Math.abs(result.getTimestamp() - timestamp) > Math.abs(p.getTimestamp() - timestamp)) {
                result = p;
            }
        }
        return result;
    }

    public SeriesFacade newFacade()
    {
        return this.new Facade();
    }

    private class Facade extends AbstractSeriesFacade
    {
        /**
         * Get a single scalar result from this series
         *
         * This is a convenience method for the common case where only 1 data point is contained per
         * device (e.g. when aggregation interval is same as the sliding interval).
         */
        @Override
        public Double get(EnumDataField field, EnumMetric metric)
        {
            if (points.size() != 1)
                return null;

            DataPoint p0 = points.get(0);
            Map<EnumMetric, Double> m = p0.field(field);
            return m != null? m.get(metric) : null;
        }

        @Override
        public Double get(EnumDataField field, EnumMetric metric, Predicate<Point> pred)
        {
            Double result = null;
            for (DataPoint datapoint: points) {
                Map<EnumMetric, Double> f = datapoint.field(field);
                if (f == null)
                    continue; // the field is not present
                Long t = datapoint.getTimestamp();
                if (t == null)
                    continue; // not paired with a timestamp
                Point point = Point.of(t, f.get(metric));
                if (pred.evaluate(point)) {
                    // A suitable point is found; check is a single one
                    if (result == null)
                        // this is the 1st time we encounter a suitable point
                        result = point.getValue();
                    else {
                        // found a duplicate: do not accept as a result
                        result = null;
                        break;
                    }
                }
            }
            return result;
        }

        /**
         * Get an iterable of (time, value) pairs from this series
         */
        @Override
        public Iterable<Point> iterPoints(EnumDataField field, EnumMetric metric)
        {
            return GroupDataSeries.this.new PointIterable(field, metric);
        }

        @Override
        public int size()
        {
            return points.size();
        }

        @Override
        public boolean isEmpty()
        {
            return points.isEmpty();
        }

        @Override
        public int getPopulationCount()
        {
            return population;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /**
     * An iterable on points of (timestamp, value) for a given (field, metric).
     */
    private class PointIterable implements Iterable<Point>
    {
        private final EnumDataField field;
        private final EnumMetric metric;

        private final int endIndex;

        public PointIterable(EnumDataField field, EnumMetric metric)
        {
            this.field = field;
            this.metric = metric;

            // Set endIndex, examine if series contains the given field
            int size = points.size();
            if (size > 0) {
                DataPoint p0 = points.get(0);
                endIndex = (p0.field(field) != null)? size : 0;
            } else {
                endIndex = 0; // empty iterable
            }
        }

        @Override
        public Iterator<Point> iterator()
        {
            return new I();
        }

        private class I implements Iterator<Point>
        {
            private int currIndex = 0;

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext()
            {
                return (currIndex < endIndex);
            }

            @Override
            public Point next()
            {
                DataPoint p = points.get(currIndex);
                currIndex++;

                Map<EnumMetric, Double> f = p.field(field);
                return Point.of(p.getTimestamp(), f.get(metric));
            }
        }
    }
}
