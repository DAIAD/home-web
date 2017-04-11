package eu.daiad.web.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.Period;

public enum EnumPartOfDay
{
    MORNING("05:00", "12:00"),
    AFTERNOON("12:00", "19:00"),
    NIGHT("19:00", "05:00");

    private final LocalTime start;

    private final LocalTime end;

    private EnumPartOfDay(String startTime, String endTime)
    {
        this(LocalTime.parse(startTime), LocalTime.parse(endTime));
    }

    private EnumPartOfDay(LocalTime start, LocalTime end)
    {
        this.start = start;
        this.end = end;
    }

    public LocalTime getStart()
    {
        return start;
    }

    public LocalTime getEnd()
    {
        return end;
    }

    private static DateTime adjustToTime(DateTime refDate, LocalTime t)
    {
        return refDate.withTime(
            t.getHourOfDay(), t.getMinuteOfHour(), t.getSecondOfMinute(), t.getMillisOfSecond()
        );
    }

    public Interval toInterval(DateTime refDate)
    {
        DateTime t0 = adjustToTime(refDate, start);
        DateTime t1 = adjustToTime(refDate, end);

        if (t0.isAfter(t1))
            t1 = t1.plusDays(1);

        return new Interval(t0, t1);
    }

    public Period toPeriod()
    {
        // Use an arbitrary instant (e.g. 0s since epoch) as reference date
        DateTime t0 = new DateTime(0);
        return toInterval(t0).toPeriod();
    }

    public double asFractionOfDay()
    {
        return ((double) toPeriod().getHours()) / DateTimeConstants.HOURS_PER_DAY;
    }
}