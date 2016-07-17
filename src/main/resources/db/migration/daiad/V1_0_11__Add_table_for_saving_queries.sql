CREATE SEQUENCE public.data_query_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.data_query
(
  id bigint NOT NULL DEFAULT nextval('data_query_id_seq'::regclass),
  account_id integer,
  name character varying(100),
  query character varying,
  date_modified timestamp without time zone,
  CONSTRAINT pk_data_query PRIMARY KEY (id),
  CONSTRAINT uq_data_query_name UNIQUE (account_id, name),
  CONSTRAINT fk_data_query_account FOREIGN KEY (account_id)
    REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
