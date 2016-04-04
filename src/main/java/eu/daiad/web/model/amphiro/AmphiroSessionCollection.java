package eu.daiad.web.model.amphiro;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.web.model.TemporalConstants;

public class AmphiroSessionCollection {

	@JsonIgnore
	private int granularity = TemporalConstants.NONE;
	
	private UUID deviceKey;

	private ArrayList<AmphiroAbstractSession> sessions;

	public AmphiroSessionCollection(UUID deviceKey, int granularity) {
		this.deviceKey = deviceKey;
		this.granularity = granularity;

		this.sessions = new ArrayList<AmphiroAbstractSession>();
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public ArrayList<AmphiroAbstractSession> getSessions() {
		return sessions;
	}

	public void addSessions(ArrayList<AmphiroSession> sessions) {
		this.sessions.clear();
		if (sessions != null) {
			for (int i = 0, count = sessions.size(); i < count; i++) {
				this.add(sessions.get(i));
			}
		}
	}
	
	public void add(AmphiroSession session) {
		if (this.granularity == TemporalConstants.NONE) {
			// Retrieve values at the highest granularity, that is at the
			// measurement level
			this.sessions.add(session);
		} else {

			DateTime date = new DateTime(session.getTimestamp(), DateTimeZone.UTC);

			switch (this.granularity) {
			case TemporalConstants.HOUR:
				date = new DateTime(date.getYear(), date.getMonthOfYear(),
						date.getDayOfMonth(), date.getHourOfDay(), 0, 0, DateTimeZone.UTC);
				break;
			case TemporalConstants.DAY:
				date = new DateTime(date.getYear(), date.getMonthOfYear(),
						date.getDayOfMonth(), 0, 0, 0, DateTimeZone.UTC);
				break;
			case TemporalConstants.WEEK:
				DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

				date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(),
						sunday.getDayOfMonth(), 0, 0, 0, DateTimeZone.UTC);
				break;
			case TemporalConstants.MONTH:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), 1,
						0, 0, 0, DateTimeZone.UTC);
				break;
			case TemporalConstants.YEAR:
				date = new DateTime(date.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
				break;
			default:
				throw new IllegalArgumentException(
						"Granularity level not supported.");
			}

			AmphiroAggregatedSession aggregate = null;

			for (int i = 0, count = this.sessions.size(); i < count; i++) {
				if (date.getMillis() == this.sessions.get(i).getTimestamp()) {
					aggregate = (AmphiroAggregatedSession) this.sessions.get(i);

					aggregate.addPoint(session);

					return;
				}
			}

			aggregate = new AmphiroAggregatedSession(date.getMillis());
			aggregate.addPoint(session);

			this.sessions.add(aggregate);
		}
	}
	
}
