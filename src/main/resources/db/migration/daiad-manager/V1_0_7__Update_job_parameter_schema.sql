-- Remove existing jobs
delete from public.scheduled_job_parameter;
delete from public.scheduled_job;

-- Separate parameters based on step
DO $$
BEGIN
  IF not EXISTS (SELECT column_name 
                 FROM information_schema.columns 
                 WHERE table_schema='public' and table_name='scheduled_job_parameter' and column_name='step') THEN
    alter table scheduled_job_parameter add column step character varying(50) not null ;
  else
    raise NOTICE 'Column [step] already exists in table [public].[scheduled_job_parameter]';
  END IF;
END
$$;

--  Update constraint
alter table scheduled_job_parameter drop constraint if exists scheduled_job_parameter_name_unique;

alter table scheduled_job_parameter add constraint scheduled_job_parameter_name_unique UNIQUE(scheduled_job_id, step, name);
