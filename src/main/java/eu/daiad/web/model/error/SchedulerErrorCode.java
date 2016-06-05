package eu.daiad.web.model.error;

public enum SchedulerErrorCode implements ErrorCode {
	SCHEDULER_JOB_LAUNCH_FAIL,
	MAPREDUCE_JOB_INIT_FAIL;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
