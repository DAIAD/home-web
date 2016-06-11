package eu.daiad.web.model.error;

public enum SchedulerErrorCode implements ErrorCode {
	SCHEDULER_JOB_LAUNCH_FAIL,
	SCHEDULER_JOB_STEP_FAIL,
	SCHEDULER_INVALID_PARAMETER,
	MAPREDUCE_JOB_INIT_FAIL;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
