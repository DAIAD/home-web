package eu.daiad.web.scheduling;

import java.util.concurrent.ScheduledFuture;

public class JobSchedulingProperties {

	private long id;

	private ScheduledFuture<?> future;

	public JobSchedulingProperties(long id, ScheduledFuture<?> future) {
		this.id = id;
		this.future = future;
	}

	public long getId() {
		return id;
	}

	public ScheduledFuture<?> getFuture() {
		return future;
	}

}
