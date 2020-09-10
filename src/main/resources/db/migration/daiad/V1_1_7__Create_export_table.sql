DROP TABLE IF EXISTS export;
DROP SEQUENCE IF EXISTS export_id_seq;

CREATE SEQUENCE export_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 270
  CACHE 1;

CREATE TABLE export
(
  id bigint NOT NULL DEFAULT nextval('export_id_seq'::regclass),
  key uuid,
  created_on timestamp without time zone not null,
  started_on timestamp without time zone not null,
  completed_on timestamp without time zone,
  path character varying not null,
  filename character varying not null,
  file_size bigint not null,
  utility_id int,
  utility_name character varying, 
  description character varying,
  row_count bigint not null,
  CONSTRAINT pk_export PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE upload
  OWNER TO daiad;
