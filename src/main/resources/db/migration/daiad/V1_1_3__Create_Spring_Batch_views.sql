-- Job executions
CREATE OR REPLACE VIEW batch.scheduled_job_execution AS
	select 	i.job_instance_id,
		i.job_name,
		e.job_execution_id,
		e.start_time,
		e.end_time,
		e.status,
		e.exit_code,
		e.exit_message
	from	batch.job_instance i
				inner join batch.job_execution e
					on i.job_instance_id = e.job_instance_id;

