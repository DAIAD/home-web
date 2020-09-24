package eu.daiad.common.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumTimeUnit 
{
	HOUR(1) {
        @Override
        public Period toPeriod()
        {
            return Period.hours(1);
        }

        @Override
        public DateTime startOf(DateTime t)
        {
            return t.withTime(t.getHourOfDay(), 0, 0, 0);
        }

        @Override
        public int numParts(Interval interval)
        {
            return interval.toPeriod(PeriodType.hours()).getHours();
        }
    },
	
	DAY(2) {
        @Override
        public Period toPeriod()
        {
            return Period.days(1);
        }

        @Override
        public DateTime startOf(DateTime t)
        {
            return t.withTimeAtStartOfDay();
        }

        @Override
        public int numParts(Interval interval)
        {
            return interval.toPeriod(PeriodType.days()).getDays();
        }
    },
	
	WEEK(3) {
        @Override
        public Period toPeriod()
        {
            return Period.weeks(1);
        }

        @Override
        public DateTime startOf(DateTime t)
        {
            return t.withTimeAtStartOfDay().withDayOfWeek(DateTimeConstants.MONDAY);
        }

        @Override
        public int numParts(Interval interval)
        {
            return interval.toPeriod(PeriodType.weeks()).getWeeks();
        }
    }, 
	
	MONTH(4) {
        @Override
        public Period toPeriod()
        {
            return Period.months(1);
        }

        @Override
        public DateTime startOf(DateTime t)
        {
            return t.withTimeAtStartOfDay().withDayOfMonth(1);
        }

        @Override
        public int numParts(Interval interval)
        {
            return interval.toPeriod(PeriodType.months()).getMonths();
        }
    },
	
	YEAR(5) {
        @Override
        public Period toPeriod()
        {
            return Period.years(1);
        }

        @Override
        public DateTime startOf(DateTime t)
        {
            return t.withTimeAtStartOfDay().withDayOfYear(1);
        }

        @Override
        public int numParts(Interval interval)
        {
            return interval.toPeriod(PeriodType.years()).getYears();
        }
    };

	private final int value;

	private EnumTimeUnit(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumTimeUnit> intToTypeMap = new HashMap<Integer, EnumTimeUnit>();
	static {
		for (EnumTimeUnit type : EnumTimeUnit.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumTimeUnit fromInteger(int value) {
		EnumTimeUnit type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumTimeUnit.HOUR;
		return type;
	}

	public static EnumTimeUnit fromString(String value) {
		for (EnumTimeUnit item : EnumTimeUnit.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumTimeUnit.HOUR;
	}

	public abstract Period toPeriod();
	
	public abstract DateTime startOf(DateTime t);
	
	public abstract int numParts(Interval interval);
	
	public static class Deserializer extends JsonDeserializer<EnumTimeUnit> {

		@Override
		public EnumTimeUnit deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumTimeUnit.fromString(parser.getValueAsString());
		}
	}

}
