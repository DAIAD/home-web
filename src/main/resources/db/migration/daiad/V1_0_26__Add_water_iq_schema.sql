DROP TABLE IF EXISTS public.water_iq_history;
DROP SEQUENCE IF EXISTS public.water_iq_history_id_seq;

DROP TABLE IF EXISTS public.water_iq;

DROP TABLE IF EXISTS public.water_iq;
-- Account current water iq information
CREATE TABLE public.water_iq
(
  id int not null,
  updated_on timestamp without time zone not null,
  interval_from character varying(8) not null,
  interval_to character varying(8) not null,
  row_version bigint default 0,
  user_volume double precision not null,
  user_value character varying(2) not null,
  similar_volume double precision null,
  similar_value character varying(2) null,
  nearest_volume double precision null,
  nearest_value character varying(2) null,
  all_volume double precision null,
  all_value character varying(2) null,
  user_1m_consumption double precision null,
  similar_1m_consumption double precision null,
  nearest_1m_consumption double precision null,
  all_1m_consumption double precision null,
  user_6m_consumption double precision null,
  similar_6m_consumption double precision null,
  nearest_6m_consumption double precision null,
  all_6m_consumption double precision null,
  CONSTRAINT pk_water_iq PRIMARY KEY (id),
  CONSTRAINT fk_water_iq_account FOREIGN KEY (id)
  REFERENCES public.account (id) MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

-- Account water iq historical information
DROP TABLE IF EXISTS public.water_iq_history;
DROP SEQUENCE IF EXISTS public.water_iq_history_id_seq;

CREATE SEQUENCE public.water_iq_history_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.water_iq_history
(
  id int not null default nextval('water_iq_history_id_seq'::regclass),
  account_id int not null,
  created_on timestamp without time zone not null,
  interval_from character varying(8) not null,
  interval_to character varying(8) not null,
  user_volume double precision not null,
  user_value character varying(2) not null,
  similar_volume double precision null,
  similar_value character varying(2) null,
  nearest_volume double precision null,
  nearest_value character varying(2) null,
  all_volume double precision null,
  all_value character varying(2) null,
  user_1m_consumption double precision null,
  similar_1m_consumption double precision null,
  nearest_1m_consumption double precision null,
  all_1m_consumption double precision null,
  user_6m_consumption double precision null,
  similar_6m_consumption double precision null,
  nearest_6m_consumption double precision null,
  all_6m_consumption double precision null,
  CONSTRAINT pk_water_iq_history PRIMARY KEY (id),
  CONSTRAINT fk_water_iq_history_account FOREIGN KEY (account_id)
  REFERENCES public.account (id) MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
