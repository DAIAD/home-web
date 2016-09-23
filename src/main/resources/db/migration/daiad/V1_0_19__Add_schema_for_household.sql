DROP TABLE IF EXISTS public.household_member;
DROP TABLE IF EXISTS public.household;

DROP SEQUENCE IF EXISTS public.household_member_id_seq;

CREATE TABLE public.household
(
  id integer NOT NULL,
  row_version  bigint NOT NULL default 1,
  created_on timestamp without time zone,
  updated_on timestamp without time zone,
  CONSTRAINT pk_household PRIMARY KEY (id),
  CONSTRAINT fk_household_account FOREIGN KEY (id)
    REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

CREATE SEQUENCE public.household_member_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
  
CREATE TABLE public.household_member (
    id integer NOT NULL DEFAULT nextval('household_member_id_seq'::regclass),
    row_version  bigint NOT NULL default 1,
    household_id integer,
    index int,
    name character varying(40),
    age integer,
    gender character varying(12),
    photo bytea,
    created_on timestamp without time zone,
    updated_on timestamp without time zone,
    CONSTRAINT pk_household_member PRIMARY KEY (id),
    CONSTRAINT fk_household_member_household FOREIGN KEY (household_id)
        REFERENCES public.household (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uq_household_member_index UNIQUE (id, index)
);

insert into household (id, created_on, updated_on)
select id, now() at time zone 'utc', now() at time zone 'utc'
from account;

insert into household_member (household_id, index, name, age, gender, photo, created_on, updated_on)
select a.id, 0, a.firstname, s.age, a.gender, a.photo, now() at time zone 'utc', now() at time zone 'utc' 
from account a left outer join survey s on a.username = s.username;
