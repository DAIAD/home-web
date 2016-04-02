-- Incrementer
DROP SEQUENCE  IF EXISTS INCREMENTER ;

-- Uploaded documents
DROP TABLE IF EXISTS public.upload;

DROP SEQUENCE IF EXISTS public.upload_id_seq;

-- Scheduling
DROP TABLE IF EXISTS public.schedule;

DROP SEQUENCE IF EXISTS public.jschedule_id_seq;

-- Registered jobs and parameters
DROP TABLE IF EXISTS public.job_parameter;

DROP TABLE IF EXISTS public.job;

DROP SEQUENCE IF EXISTS public.job_id_seq;

DROP SEQUENCE IF EXISTS public.job_parameter_id_seq;
