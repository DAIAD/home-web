CREATE SEQUENCE public.log4j_message_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;



CREATE TABLE public.log4j_message
(
  id bigint NOT NULL DEFAULT nextval('log4j_message_id_seq'::regclass),
  account character varying(100),
  remote_address character varying(100),
  category character varying(100),
  code character varying(100),
  level character varying(10),
  logger character varying(512),
  message character varying,
  exception character varying,
  timestamp timestamp without time zone
)
WITH (
  OIDS=FALSE
);
