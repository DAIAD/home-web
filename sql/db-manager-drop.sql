-- Incrementer
DROP SEQUENCE  IF EXISTS INCREMENTER ;

-- Uploaded documents
DROP TABLE IF EXISTS public.upload;

DROP SEQUENCE IF EXISTS public.upload_id_seq;

-- Registered jobs and parameters
DROP TABLE IF EXISTS public.scheduled_job_parameter;

DROP TABLE IF EXISTS public.scheduled_job;

DROP TABLE IF EXISTS public.job_execution_parameter;

DROP TABLE IF EXISTS public.job_execution;

DROP TABLE IF EXISTS public.job;

DROP SEQUENCE IF EXISTS public.job_execution_parameter_id_seq;

DROP SEQUENCE IF EXISTS public.job_execution_id_seq;

DROP SEQUENCE IF EXISTS public.scheduled_job_parameter_id_seq;

DROP SEQUENCE IF EXISTS public.scheduled_job_id_seq;

DROP SEQUENCE IF EXISTS public.job_id_seq;
