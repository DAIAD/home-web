--  utility
CREATE SEQUENCE utility_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE utility (
    id integer DEFAULT nextval('utility_id_seq'::regclass) NOT NULL,
    name character varying(40),
    logo bytea,
    description character varying,
    date_created timestamp without time zone,
    CONSTRAINT pk_utility PRIMARY KEY (id)
);

-- account
CREATE SEQUENCE account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE account (
    id integer NOT NULL DEFAULT nextval('account_id_seq'::regclass),
    utility_id integer,
    key uuid,
    firstname character varying(40),
    lastname character varying(70),   
    email character varying(100),
    created_on timestamp without time zone,
    last_login_success timestamp without time zone,
    last_login_failure timestamp without time zone,
    failed_login_attempts integer,
    change_password_on_login boolean DEFAULT false NOT NULL,
    locked boolean DEFAULT true NOT NULL,
    username character varying(100) NOT NULL,    
    password character varying(100),
    photo bytea,
	timezone character varying(50),
	country character varying(50),
	postal_code character varying(10),
	birthdate timestamp without time zone,
	gender character varying(12),    
    CONSTRAINT pk_account PRIMARY KEY (id),
    CONSTRAINT fk_utility FOREIGN KEY (utility_id)
        REFERENCES public.utility (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE SET NULL
);

-- role
CREATE SEQUENCE role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE role (
    id integer NOT NULL DEFAULT nextval('role_id_seq'::regclass),
    name character varying(50),
    description character varying(100),
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE SEQUENCE account_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
CREATE TABLE account_role (
    id integer NOT NULL DEFAULT nextval('account_role_id_seq'::regclass),
    account_id integer,
    role_id integer,
    date_assigned timestamp without time zone,
    assigned_by integer,
    CONSTRAINT pk_account_role PRIMARY KEY (id),
    CONSTRAINT fk_account_role_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_account_role_role FOREIGN KEY (role_id)
        REFERENCES public.role (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE  
);

-- device
CREATE SEQUENCE public.device_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
  
CREATE TABLE public.device
(
  id integer NOT NULL DEFAULT nextval('device_id_seq'::regclass),
  key uuid,
  account_id integer,
  CONSTRAINT pk_device PRIMARY KEY (id),
  CONSTRAINT fk_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE device_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

 CREATE TABLE device_property (
    id integer NOT NULL DEFAULT nextval('device_property_id_seq'::regclass),
    device_id integer,
    key character varying(50),
    value character varying,
    CONSTRAINT pk_device_property PRIMARY KEY (id),
    CONSTRAINT fk_device_property_device FOREIGN KEY (device_id)
        REFERENCES public.device (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- amphiro
CREATE TABLE public.device_amphiro
(
  id integer NOT NULL,
  name character varying(50),  
  mac_address character varying(100),
  CONSTRAINT pk_device_amphiro PRIMARY KEY (id),
  CONSTRAINT fk_device_amphiro_device FOREIGN KEY (id)
        REFERENCES public.device (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE device_amphiro_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
CREATE TABLE device_amphiro_config (
    id integer NOT NULL DEFAULT nextval('device_amphiro_config_id_seq'::regclass),
    device_id integer,
    key character varying(50),
    value character varying,
    type character(20),
    CONSTRAINT pk_device_amphiro_config PRIMARY KEY (id),
    CONSTRAINT fk_device_amphiro_config_device_amphiro FOREIGN KEY (device_id)
        REFERENCES public.device_amphiro (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- meter
CREATE TABLE public.device_meter
(
  id integer NOT NULL,
  serial character varying(50),  
  CONSTRAINT pk_device_meter PRIMARY KEY (id),
  CONSTRAINT fk_device_meter_device FOREIGN KEY (id)
        REFERENCES public.device (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- community
CREATE SEQUENCE community_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE community (
    id integer NOT NULL DEFAULT nextval('community_id_seq'::regclass),
    name character varying(100),
    created_on timestamp without time zone,
    description character varying,
    image bytea,
    spatial geometry,
    size integer,
    CONSTRAINT pk_community PRIMARY KEY (id),
    CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(spatial) = 2),
    CONSTRAINT enforce_srid_the_geom CHECK (st_srid(spatial) = 4326)
);

CREATE SEQUENCE community_member_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
CREATE TABLE community_member (
    id integer NOT NULL DEFAULT nextval('community_member_id_seq'::regclass),
    community_id integer,
    account_id integer,
    created_on timestamp without time zone,
    ranking integer,
    CONSTRAINT pk_community_member PRIMARY KEY (id),
    CONSTRAINT fk_community_member_community FOREIGN KEY (community_id)
        REFERENCES public.community (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_community_member_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE        
);

-- group
CREATE SEQUENCE group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE "group" (
    id integer NOT NULL DEFAULT nextval('group_id_seq'::regclass),
    name character varying(100),
    created_on timestamp without time zone,
    reference integer,
    CONSTRAINT pk_group PRIMARY KEY (id)
);

CREATE SEQUENCE group_member_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE group_member (
    id integer NOT NULL DEFAULT nextval('group_member_id_seq'::regclass),
    group_id integer,
    account_id integer,
    created_on timestamp without time zone,
    CONSTRAINT pk_group_member PRIMARY KEY (id),
    CONSTRAINT fk_group_member_group FOREIGN KEY (group_id)
        REFERENCES public."group" (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_group_member_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE   
);
