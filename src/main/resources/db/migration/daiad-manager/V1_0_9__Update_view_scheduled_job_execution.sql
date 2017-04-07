DROP VIEW batch.scheduled_job_execution;

CREATE OR REPLACE VIEW batch.scheduled_job_execution AS 
SELECT i.job_instance_id,
    j.id AS job_id,
    i.job_key,
    i.job_name,
    e.job_execution_id,
    e.start_time,
    e.end_time,
    e.status,
    e.exit_code,
    e.exit_message
   FROM batch.job_instance i
     JOIN batch.job_execution e ON i.job_instance_id = e.job_instance_id
     LEFT JOIN scheduled_job j ON i.job_name::text = j.name::text;
