package eu.daiad.common.model.logging;

import java.util.List;

import eu.daiad.common.domain.application.LogEventEntity;

public class LogEventQueryResult {

	private List<LogEventEntity> events;

	private int total;

	public List<LogEventEntity> getEvents() {
		return events;
	}

	public void setEvents(List<LogEventEntity> events) {
		this.events = events;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
