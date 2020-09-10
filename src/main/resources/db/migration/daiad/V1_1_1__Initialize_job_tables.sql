-- Schema for Spring Batch objects
CREATE SCHEMA IF NOT EXISTS batch;

-- Job parameters incrementer
CREATE SEQUENCE INCREMENTER MAXVALUE 9223372036854775807 NO CYCLE;

-- Scheduled jobs
CREATE SEQUENCE scheduled_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE scheduled_job (
    id bigint DEFAULT nextval('scheduled_job_id_seq'::regclass) NOT NULL,
    category character varying(20) NOT NULL,
    container character varying(20) NOT NULL,
    bean character varying,
    name character varying(200),
    description character varying,
    date_created timestamp without time zone,
    period bigint,
    cron_expression character varying(40),
    enabled boolean not null,
    CONSTRAINT pk_scheduled_job PRIMARY KEY (id),
    CONSTRAINT scheduled_job_name_unique UNIQUE (name)
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
    hidden boolean NOT NULL,
    CONSTRAINT pk_scheduled_job_parameter PRIMARY KEY (id),
    CONSTRAINT fk_scheduled_job_parameter_scheduled_job FOREIGN KEY (scheduled_job_id)
        REFERENCES public.scheduled_job (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT scheduled_job_parameter_name_unique UNIQUE (scheduled_job_id, name)
);
