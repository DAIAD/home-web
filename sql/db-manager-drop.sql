-- Uploaded documents
DROP TABLE IF EXISTS public.upload;

DROP SEQUENCE IF EXISTS public.upload_id_seq;

-- Incrementer
DROP SEQUENCE  IF EXISTS INCREMENTER ;

-- Registered jobs and parameters
DROP TABLE IF EXISTS public.scheduled_job_parameter;

DROP TABLE IF EXISTS public.scheduled_job;

DROP SEQUENCE IF EXISTS public.scheduled_job_parameter_id_seq;

DROP SEQUENCE IF EXISTS public.scheduled_job_id_seq;
