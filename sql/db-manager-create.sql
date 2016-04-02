-- Incrementer
CREATE SEQUENCE INCREMENTER MAXVALUE 9223372036854775807 NO CYCLE;

-- Uploaded documents
CREATE SEQUENCE upload_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE upload (
    id integer DEFAULT nextval('upload_id_seq'::regclass) NOT NULL,
    source character varying,
    remote_folder character varying,
    local_folder character varying,
    remote_filename character varying,
    local_filename character varying,
    file_size bigint,
    date_modified timestamp without time zone,    
	upload_start_on timestamp without time zone,
    upload_end_on timestamp without time zone,
    process_start_on timestamp without time zone,
    process_end_on timestamp without time zone,
    row_processed int,
    row_skipped int,
    CONSTRAINT pk_upload PRIMARY KEY (id)
);

-- Registered jobs and parameters
CREATE SEQUENCE job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE job (
    id integer DEFAULT nextval('job_id_seq'::regclass) NOT NULL,
    bean_name character varying,
	job_name character varying(200),
    job_description character varying,
    date_created timestamp without time zone,
    CONSTRAINT pk_job PRIMARY KEY (id),
    CONSTRAINT job_name_unique UNIQUE (job_name)
);

CREATE SEQUENCE job_parameter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
CREATE TABLE job_parameter (
    id integer NOT NULL DEFAULT nextval('job_parameter_id_seq'::regclass),
    job_id integer,
    name character varying(50),
    value character varying,
    CONSTRAINT pk_job_parameter PRIMARY KEY (id),
    CONSTRAINT fk_job_parameter_job FOREIGN KEY (job_id)
        REFERENCES public.job (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT job_parameter_name_unique UNIQUE (name)
);

-- Scheduling
CREATE SEQUENCE jschedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE schedule (
    id integer DEFAULT nextval('jschedule_id_seq'::regclass) NOT NULL,
    job_id int NOT NULL,
    period bigint,
	cron character varying(40),
    start_on timestamp without time zone,
    CONSTRAINT pk_schedule PRIMARY KEY (id),
	CONSTRAINT fk_schedule_job FOREIGN KEY (job_id)
        REFERENCES public.job (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);
