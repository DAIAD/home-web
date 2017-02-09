-- Add new column to "group" table
alter table public."group" add updated_on timestamp without time zone;

-- Rename commons table
DROP TABLE IF EXISTS public.group_community;

CREATE TABLE public.group_commons
(
  id integer NOT NULL,
  description character varying,
  image bytea,
  owner_id integer not null,
  CONSTRAINT pk_group_commons PRIMARY KEY (id),
  CONSTRAINT fk_group_commons_account FOREIGN KEY (owner_id)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_group_commons_group FOREIGN KEY (id)
      REFERENCES public."group" (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
