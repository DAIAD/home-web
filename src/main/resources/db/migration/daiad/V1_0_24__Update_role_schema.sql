--  Increase field length
alter table public."role" alter column description type character varying(200);

-- Drop existing objects
DROP TABLE IF EXISTS public.account_utility;
DROP SEQUENCE IF EXISTS public.account_utility_id_seq;

-- Create sequence account_utility_id_seq
CREATE SEQUENCE public.account_utility_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 226
  CACHE 1;

-- Create table account_utility
CREATE TABLE public.account_utility
(
  id integer NOT NULL DEFAULT nextval('account_utility_id_seq'::regclass),
  account_id integer,
  utility_id integer,
  date_assigned timestamp without time zone,
  CONSTRAINT pk_account_utility PRIMARY KEY (id),
  CONSTRAINT fk_account_utility_account FOREIGN KEY (account_id)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_account_utility_utility FOREIGN KEY (utility_id)
      REFERENCES public.utility (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

--  Drop existing roles
delete from account_role where role_id in (select id from "role" where name = 'ROLE_SUPERUSER');
delete from "role" where name = 'ROLE_SUPERUSER';

--  Rename existing roles
update "role" set name = 'ROLE_SYSTEM_ADMIN' where name ='ROLE_ADMIN';

-- Insert default utilities
insert into account_utility (account_id, utility_id, date_assigned)
select id, utility_id, now() at time zone 'utc'
from account;
