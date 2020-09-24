package eu.daiad.common.model.logging;

import java.util.List;

import eu.daiad.common.model.RestResponse;

public class LogEventQueryResponse extends RestResponse {

	private int total = 0;

	private int index;

	private int size;

	private List<LogEvent> events;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<LogEvent> getEvents() {
		return events;
	}

	public void setEvents(List<LogEvent> events) {
		this.events = events;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
