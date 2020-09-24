-- Drop all objects
DROP TABLE IF EXISTS public.budget_account_snapshot;
DROP SEQUENCE IF EXISTS public.budget_account_snapshot_id_seq;

DROP TABLE IF EXISTS public.budget_snapshot;
DROP SEQUENCE IF EXISTS public.budget_snapshot_id_seq;

DROP TABLE IF EXISTS public.budget_account;
DROP SEQUENCE IF EXISTS public.budget_account_id_seq;

DROP TABLE IF EXISTS public.budget;
DROP SEQUENCE IF EXISTS public.budget_id_seq;

-- Budget
CREATE SEQUENCE public.budget_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.budget
(
  id bigint not null default nextval('budget_id_seq'::regclass),
  row_version bigint not null default 1,
  key uuid not null,
  utility_id int not null,
  account_id int not null,
  scenario_id bigint,
  scenario_percent int,
  budget_goal_percent int,
  budget_distribution character varying(20) not null,
  created_on timestamp without time zone not null,
  updated_on timestamp without time zone,
  next_update_on timestamp without time zone,
  name character varying not null,
  "parameters" character varying not null,
  active boolean not null,
  activated_on timestamp without time zone,
  number_of_consumers int,
  expected_savings_percent double precision,
  initialized boolean not null,
  CONSTRAINT pk_budget PRIMARY KEY (id),
  CONSTRAINT fk_utility FOREIGN KEY (utility_id)
      REFERENCES public.utility (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_account FOREIGN KEY (account_id)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_scenario FOREIGN KEY (scenario_id)
      REFERENCES public.savings_potential_scenario (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

CREATE SEQUENCE public.budget_snapshot_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.budget_snapshot
(
  id bigint not null default nextval('budget_snapshot_id_seq'::regclass),
  budget_id bigint not null,
  job_id bigint,
  created_on timestamp without time zone not null,
  processing_start timestamp without time zone,
  processing_end timestamp without time zone,
  status character varying(50) not null,
  "year" int not null,
  "month" int not null,
  consumption_volume_before double precision,
  consumption_volume_after double precision,
  savings_percent double precision,
  expected_savings_percent double precision,
  CONSTRAINT pk_budget_snapshot PRIMARY KEY (id),
  CONSTRAINT fk_budget FOREIGN KEY (budget_id)
      REFERENCES public.budget (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

-- Consumers
CREATE SEQUENCE public.budget_account_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.budget_account
(
  id bigint not null default nextval('budget_account_id_seq'::regclass),
  budget_id bigint not null,
  account_id int not null,
  CONSTRAINT pk_budget_account PRIMARY KEY (id),
  CONSTRAINT fk_budget FOREIGN KEY (budget_id)
      REFERENCES public.budget (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_account FOREIGN KEY (account_id)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

CREATE SEQUENCE public.budget_account_snapshot_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.budget_account_snapshot
(
  id bigint not null default nextval('budget_account_snapshot_id_seq'::regclass),
  budget_snapshot_id bigint not null,
  account_id int not null,
  created_on timestamp without time zone not null,
  consumption_volume_before double precision,
  consumption_volume_after double precision,
  savings_percent double precision,
  expected_savings_percent double precision,
  CONSTRAINT pk_budget_account_snapshot PRIMARY KEY (id),
  CONSTRAINT fk_budget_snapshot FOREIGN KEY (budget_snapshot_id)
      REFERENCES public.budget_snapshot (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_account FOREIGN KEY (account_id)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
