package eu.daiad.web.model.error;

public enum SchedulerErrorCode implements ErrorCode {
	SCHEDULER_JOB_LAUNCH_FAILED,
	SCHEDULER_JOB_STEP_FAILED,
	SCHEDULER_INVALID_PARAMETER,

	MAPREDUCE_JOB_INIT_FAILED,
	FLINK_JOB_INIT_FAILED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + name());
	}
}
