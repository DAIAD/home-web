package eu.daiad.common.model.query;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.functors.NotPredicate;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

/**
 * Represent a pair of (timestamp, value).
 * This representation is agnostic to the nature of the referenced value.
 */
public class Point
{
    private final Instant instant;
    private final Double value;

    private Point(Instant instant, Double value)
    {
        this.instant = instant;
        this.value = value;
    }

    //
    // ~ Methods
    //

    public Instant getTimestamp()
    {
        return instant;
    }

    public Double getValue()
    {
        return value;
    }

    public static Point of(long timestamp, Double value)
    {
        return new Point(new Instant(timestamp), value);
    }

    public static Point of(Instant instant, Double value)
    {
        return new Point(instant, value);
    }

    @Override
    public String toString()
    {
        return "Point [instant=" + instant + ", value=" + value + "]";
    }

    //
    // ~ Predicates
    //

    private static class BetweenTimePredicate implements Predicate<Point>
    {
        private final DateTime start, end;

        public BetweenTimePredicate(DateTime start, DateTime end)
        {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean evaluate(Point p)
        {
            return !(p.instant.isBefore(start) || !p.instant.isBefore(end));
        }
    }

    private static class AfterTimePredicate implements Predicate<Point>
    {
        private final DateTime start;

        public AfterTimePredicate(DateTime start)
        {
            this.start = start;
        }

        @Override
        public boolean evaluate(Point p)
        {
            return p.instant.isAfter(start);
        }
    }

    private static class BeforeTimePredicate implements Predicate<Point>
    {
        private final DateTime start;

        public BeforeTimePredicate(DateTime start)
        {
            this.start = start;
        }

        @Override
        public boolean evaluate(Point p)
        {
            return p.instant.isBefore(start);
        }
    }

    private static class NotNullPredicate implements Predicate<Point>
    {
        @Override
        public boolean evaluate(Point p)
        {
            return (p.value != null);
        }
    }

    private static class BelowValuePredicate implements Predicate<Point>
    {
        private final double refValue;

        public BelowValuePredicate(double refValue)
        {
            this.refValue = refValue;
        }

        @Override
        public boolean evaluate(Point p)
        {
            return (p.value < refValue);
        }
    }

    private static class AboveValuePredicate implements Predicate<Point>
    {
        private final double refValue;

        public AboveValuePredicate(double refValue)
        {
            this.refValue = refValue;
        }

        @Override
        public boolean evaluate(Point p)
        {
            return (p.value > refValue);
        }
    }

    private static class BetweenValuePredicate implements Predicate<Point>
    {
        private final double lowValue, highValue;

        public BetweenValuePredicate(double lowValue, double highValue)
        {
            this.lowValue = lowValue;
            this.highValue = highValue;
        }

        @Override
        public boolean evaluate(Point p)
        {
            return (p.value >= lowValue && p.value < highValue);
        }
    }

    private static class EqualsValuePredicate implements Predicate<Point>
    {
        private final double refValue;

        public EqualsValuePredicate(double refValue)
        {
            this.refValue = refValue;
        }

        @Override
        public boolean evaluate(Point p)
        {
            return (Double.compare(p.value, refValue) == 0);
        }
    }

    public static Predicate<Point> afterTime(DateTime start)
    {
        return new AfterTimePredicate(start);
    }

    public static Predicate<Point> beforeTime(DateTime start)
    {
        return new BeforeTimePredicate(start);
    }

    public static Predicate<Point> betweenTime(DateTime start, DateTime end)
    {
        return new BetweenTimePredicate(start, end);
    }

    public static Predicate<Point> betweenTime(Interval interval)
    {
        return new BetweenTimePredicate(interval.getStart(), interval.getEnd());
    }

    public static Predicate<Point> aboveValue(double refValue)
    {
        return new AboveValuePredicate(refValue);
    }

    public static Predicate<Point> belowValue(double refValue)
    {
        return new BelowValuePredicate(refValue);
    }

    public static Predicate<Point> betweenValue(double lowValue, double highValue)
    {
        return new BetweenValuePredicate(lowValue, highValue);
    }

    public static Predicate<Point> equalsValue(double refValue)
    {
        return new EqualsValuePredicate(refValue);
    }

    private static Predicate<Point> ZERO_PREDICATE = new EqualsValuePredicate(0.0);

    private static Predicate<Point> NOT_ZERO_PREDICATE = new NotPredicate<>(ZERO_PREDICATE);

    public static Predicate<Point> zero()
    {
        return ZERO_PREDICATE;
    }

    public static Predicate<Point> notZero()
    {
        return NOT_ZERO_PREDICATE;
    }

    private static Predicate<Point> NOT_NULL_PREDICATE = new NotNullPredicate();

    public static Predicate<Point> notNull()
    {
        return NOT_NULL_PREDICATE;
    }
}
