-- Documents uploaded from remote services
CREATE SEQUENCE upload_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE upload (
    id bigint DEFAULT nextval('upload_id_seq'::regclass) NOT NULL,
    source character varying,
    remote_folder character varying,
    local_folder character varying,
    remote_filename character varying,
    local_filename character varying,
    file_size bigint,
    date_modified timestamp without time zone,
    upload_start_on timestamp without time zone,
    upload_end_on timestamp without time zone,
    process_start_on timestamp without time zone,
    process_end_on timestamp without time zone,
    row_count bigint,
    row_processed bigint,
    row_skipped bigint,
    CONSTRAINT pk_upload PRIMARY KEY (id)
);
