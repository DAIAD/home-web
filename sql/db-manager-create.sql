-- Documents uploaded from remote services
CREATE SEQUENCE upload_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE upload (
    id bigint DEFAULT nextval('upload_id_seq'::regclass) NOT NULL,
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
    row_count bigint,
    row_processed bigint,
    row_skipped bigint,
    CONSTRAINT pk_upload PRIMARY KEY (id)
);

-- Job parameters incrementer
CREATE SEQUENCE INCREMENTER MAXVALUE 9223372036854775807 NO CYCLE;

-- Registered jobs
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
    category character varying(20) NOT NULL,
    container character varying(20) NOT NULL,
    enabled boolean NOT NULL,
    CONSTRAINT pk_job PRIMARY KEY (id),
    CONSTRAINT job_name_unique UNIQUE (job_name)
);

-- Job execution
CREATE SEQUENCE job_execution_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE job_execution (
    id bigint DEFAULT nextval('job_execution_id_seq'::regclass) NOT NULL,
    job_id integer,
    batch_execution_id bigint,
    account_id integer NOT NULL,
    username character varying(100) NOT NULL,
    description character varying(200) NOT NULL,
    date_created timestamp without time zone,
    CONSTRAINT pk_job_execution PRIMARY KEY (id),
    CONSTRAINT fk_job_execution_job FOREIGN KEY (job_id)
        REFERENCES public.job (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE job_execution_parameter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE job_execution_parameter (
    id bigint NOT NULL DEFAULT nextval('job_execution_parameter_id_seq'::regclass),
    job_execution_id bigint,
    name character varying(50),
    value character varying,
    CONSTRAINT pk_job_execution_parameter PRIMARY KEY (id),
    CONSTRAINT fk_job_execution_parameter_job_execution FOREIGN KEY (job_execution_id)
        REFERENCES public.job_execution (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT job_execution_parameter_name_unique UNIQUE (job_execution_id, name)
);

-- Scheduled jobs
CREATE SEQUENCE scheduled_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE scheduled_job (
    id bigint DEFAULT nextval('scheduled_job_id_seq'::regclass) NOT NULL,
    job_id integer,
    period bigint,
    cron_expression character varying(40),
    CONSTRAINT pk_scheduled_job PRIMARY KEY (id),
    CONSTRAINT fk_scheduled_job_job FOREIGN KEY (job_id)
        REFERENCES public.job (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE scheduled_job_parameter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE scheduled_job_parameter (
    id bigint NOT NULL DEFAULT nextval('scheduled_job_parameter_id_seq'::regclass),
    scheduled_job_id bigint,
    name character varying(50),
    value character varying,
    CONSTRAINT pk_scheduled_job_parameter PRIMARY KEY (id),
    CONSTRAINT fk_scheduled_job_parameter_scheduled_job FOREIGN KEY (scheduled_job_id)
        REFERENCES public.scheduled_job (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT scheduled_job_parameter_name_unique UNIQUE (scheduled_job_id, name)
);
