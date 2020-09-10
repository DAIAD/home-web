DROP VIEW IF EXISTS batch.scheduled_job_execution;

CREATE OR REPLACE VIEW batch.scheduled_job_execution AS 
 SELECT i.job_instance_id,
    j.id job_id,
    i.job_name,
    e.job_execution_id,
    e.start_time,
    e.end_time,
    e.status,
    e.exit_code,
    e.exit_message
   FROM batch.job_instance i 
          JOIN batch.job_execution e ON i.job_instance_id = e.job_instance_id
          LEFT OUTER JOIN public.scheduled_job j on i.job_name = j.name;
        
