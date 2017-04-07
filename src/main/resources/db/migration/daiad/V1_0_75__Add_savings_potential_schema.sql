-- Drop all objects
DROP TABLE IF EXISTS public.savings_potential_account;
DROP SEQUENCE IF EXISTS public.savings_potential_account_id_seq;

DROP TABLE IF EXISTS public.savings_potential_water_iq;
DROP SEQUENCE IF EXISTS public.savings_potential_water_iq_id_seq;

DROP TABLE IF EXISTS public.savings_potential_result;
DROP SEQUENCE IF EXISTS public.savings_potential_result_id_seq;

DROP TABLE IF EXISTS public.savings_potential_scenario;
DROP SEQUENCE IF EXISTS public.savings_potential_scenario_id_seq;

-- Savings potential scenarios
CREATE SEQUENCE public.savings_potential_scenario_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.savings_potential_scenario
(
  id bigint not null default nextval('savings_potential_scenario_id_seq'::regclass),
  row_version bigint not null default 1,
  key uuid not null,
  job_id bigint,
  utility_id int not null,
  account_id int not null,
  created_on timestamp without time zone not null,
  name character varying not null,
  "parameters" character varying not null,
  savings_volume double precision,
  savings_percent double precision,
  consumption_volume double precision,
  processing_start timestamp without time zone,
  processing_end timestamp without time zone,
  status character varying(50) not null,
  CONSTRAINT pk_savings_potential_scenario PRIMARY KEY (id),
  CONSTRAINT fk_utility FOREIGN KEY (utility_id)
      REFERENCES public.utility (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_account FOREIGN KEY (account_id)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

-- Savings potential algorithm results
CREATE SEQUENCE public.savings_potential_result_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.savings_potential_result
(
  id bigint not null default nextval('savings_potential_result_id_seq'::regclass),
  scenario_id bigint not null,
  created_on timestamp without time zone not null,
  "cluster" character varying(50) not null,
  "month" int not null,
  serial character varying(50) not null,
  savings_percent double precision not null,
  savings_volume double precision not null,
  cluster_size int not null, 
  iq character varying(2) not null,
  deviation double precision null,
  CONSTRAINT pk_savings_potential_result PRIMARY KEY (id),
  CONSTRAINT fk_scenario FOREIGN KEY (scenario_id)
      REFERENCES public.savings_potential_scenario (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

-- Savings potential algorithm savings per account
CREATE SEQUENCE public.savings_potential_account_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.savings_potential_account
(
  id bigint not null default nextval('savings_potential_account_id_seq'::regclass),
  scenario_id bigint not null,
  account_id int not null,
  created_on timestamp without time zone not null,
  savings_percent double precision not null,
  savings_volume double precision not null,
  consumption double precision,
  CONSTRAINT pk_savings_potential_account PRIMARY KEY (id),
  CONSTRAINT fk_scenario FOREIGN KEY (scenario_id)
      REFERENCES public.savings_potential_scenario (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_account FOREIGN KEY (account_id)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

-- Savings potential algorithm results for Water IQ
CREATE SEQUENCE public.savings_potential_water_iq_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.savings_potential_water_iq
(
  id bigint not null default nextval('savings_potential_water_iq_id_seq'::regclass),
  job_id bigint not null,
  utility_id int not null,
  created_on timestamp without time zone not null,
  "month" int not null,
  serial character varying(50) not null,
  iq character varying(2) not null,
  CONSTRAINT pk_savings_potential_water_iq PRIMARY KEY (id),
  CONSTRAINT fk_utility FOREIGN KEY (utility_id)
      REFERENCES public.utility (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
