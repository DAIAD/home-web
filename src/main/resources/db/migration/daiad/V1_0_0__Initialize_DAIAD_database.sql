-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE EXTENSION IF NOT EXISTS postgis;

--  utility
CREATE SEQUENCE utility_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE utility (
    id integer DEFAULT nextval('utility_id_seq'::regclass) NOT NULL,
    key uuid,
    name character varying(40),
    logo bytea,
    description character varying,
    date_created timestamp without time zone,
    default_admin_username character varying(100) NOT NULL,
	locale character(2),
	timezone character varying(50),
	country character varying(50),
	city character varying(60),
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
    row_version  bigint default 1,
    utility_id integer,
    key uuid,
	locale character(2),
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
	city character varying(60),
	address character varying(90),
	postal_code character varying(10),
	birthdate timestamp without time zone,
	gender character varying(12),
	location geometry,
    CONSTRAINT pk_account PRIMARY KEY (id),
    CONSTRAINT fk_utility FOREIGN KEY (utility_id)
        REFERENCES public.utility (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE SET NULL,
	CONSTRAINT enforce_dims_location CHECK (st_ndims(location) = 2),
    CONSTRAINT enforce_srid_location CHECK (st_srid(location) = 4326)
);

-- profile
CREATE TABLE public.account_profile
(
  id integer,
  row_version  bigint default 1,
  version uuid NOT NULL,
  updated_on timestamp without time zone NOT NULL,
  mobile_mode int NOT NULL,
  mobile_config character varying NULL,
  web_mode int NOT NULL,
  web_config character varying NULL,
  utility_mode int NOT NULL,
  utility_config character varying NULL,
  static_tip_sent_on timestamp without time zone,
  CONSTRAINT pk_account_profile PRIMARY KEY (id),
  CONSTRAINT fk_account_profile_account FOREIGN KEY (id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE public.account_profile_history_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.account_profile_history
(
  id integer NOT NULL DEFAULT nextval('account_profile_history_id_seq'::regclass),
  profile_id int NOT NULL,
  version uuid NOT NULL,
  updated_on timestamp without time zone NOT NULL,
  acknowledged_on timestamp without time zone,
  enabled_on timestamp without time zone,
  mobile_mode int NOT NULL,
  web_mode int NOT NULL,
  utility_mode int NOT NULL,
  CONSTRAINT pk_account_profile_history PRIMARY KEY (id),
  CONSTRAINT fk_pk_account_profile_history_pk_account_profile FOREIGN KEY (profile_id)
        REFERENCES public.account_profile (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- account white list
CREATE SEQUENCE public.account_white_list_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.account_white_list
(
  id integer NOT NULL DEFAULT nextval('account_white_list_id_seq'::regclass),
  row_version  bigint default 1,
  utility_id integer,
  account_id integer,
  username character varying(100),
  registered_on timestamp without time zone,
  locale character(2),
  firstname character varying(40),
  lastname character varying(70),
  timezone character varying(50),
  country character varying(50),
  city character varying(60),
  address character varying(90),
  postal_code character varying(10),
  birthdate timestamp without time zone,
  gender character varying(12),
  default_mobile_mode integer NOT NULL,
  default_web_mode integer NOT NULL,
  location geometry,
  meter_serial character varying(50),
  meter_location geometry,
  CONSTRAINT pk_account_white_list PRIMARY KEY (id),
    CONSTRAINT fk_utility FOREIGN KEY (utility_id)
        REFERENCES public.utility (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT uq_utility_username UNIQUE (utility_id, username),
  CONSTRAINT enforce_dims_meter_location CHECK (st_ndims(meter_location) = 2),
  CONSTRAINT enforce_srid_meter_location CHECK (st_srid(meter_location) = 4326),
  CONSTRAINT enforce_dims_location CHECK (st_ndims(location) = 2),
  CONSTRAINT enforce_srid_location CHECK (st_srid(location) = 4326)
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
  row_version  bigint default 1,
  key uuid,
  account_id integer,
  registered_on timestamp without time zone,
  last_upload_success_on timestamp without time zone,
  last_upload_failure_on timestamp without time zone,
  transmission_count bigint,
  transmission_interval_sum bigint,
  transmission_interval_max integer,
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
  aes_key  character varying,
  CONSTRAINT pk_device_amphiro PRIMARY KEY (id),
  CONSTRAINT fk_device_amphiro_device FOREIGN KEY (id)
        REFERENCES public.device (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE public.device_amphiro_permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.device_amphiro_permission
(
  id integer NOT NULL DEFAULT nextval('device_amphiro_permission_id_seq'::regclass),
  device_id integer,
  owner_id integer,
  assignee_id integer,
  date_assigned timestamp without time zone,
  CONSTRAINT pk_device_amphiro_permission PRIMARY KEY (id),
  CONSTRAINT fk_device_amphiro_permission_device_amphiro FOREIGN KEY (device_id)
        REFERENCES public.device_amphiro (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_device_owner_id FOREIGN KEY (owner_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_device_assignee_id FOREIGN KEY (assignee_id)
        REFERENCES public.account (id) MATCH SIMPLE
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
    row_version  bigint default 1,
    device_id integer,
    version uuid NOT NULL,
	title character varying(100),
	created_on timestamp without time zone,
	acknowledged_on timestamp without time zone,
	enabled_on timestamp without time zone,
	active boolean,
    configuration_block integer,
    value_1 integer,
    value_2 integer,
    value_3 integer,
    value_4 integer,
    value_5 integer,
    value_6 integer,
    value_7 integer,
    value_8 integer,
    value_9 integer,
    value_10 integer,
    value_11 integer,
    value_12 integer,
    frame_number integer,
    frame_duration integer,
    CONSTRAINT pk_device_amphiro_config PRIMARY KEY (id),
    CONSTRAINT fk_device_amphiro_config_device_amphiro FOREIGN KEY (device_id)
        REFERENCES public.device_amphiro (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE device_amphiro_config_default (
    id integer NOT NULL,
	title character varying(100),
	created_on timestamp without time zone,
    configuration_block integer,
    value_1 integer,
    value_2 integer,
    value_3 integer,
    value_4 integer,
    value_5 integer,
    value_6 integer,
    value_7 integer,
    value_8 integer,
    value_9 integer,
    value_10 integer,
    value_11 integer,
    value_12 integer,
    frame_number integer,
    frame_duration integer,
    CONSTRAINT pk_device_amphiro_config_default PRIMARY KEY (id)
);

-- meter
CREATE TABLE public.device_meter
(
  id integer NOT NULL,
  serial character varying(50),
  location geometry,
  CONSTRAINT pk_device_meter PRIMARY KEY (id),
  CONSTRAINT fk_device_meter_device FOREIGN KEY (id)
        REFERENCES public.device (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT enforce_dims_location CHECK (st_ndims(location) = 2),
  CONSTRAINT enforce_srid_location CHECK (st_srid(location) = 4326)
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
    key uuid,
    row_version  bigint default 1,
    utility_id integer,
    name character varying(100),
    created_on timestamp without time zone,
    spatial geometry,
    size integer,
    CONSTRAINT pk_group PRIMARY KEY (id),
    CONSTRAINT uq_group_name_utility UNIQUE (utility_id, name),
    CONSTRAINT enforce_dims_spatial CHECK (st_ndims(spatial) = 2),
    CONSTRAINT enforce_srid_spatial CHECK (st_srid(spatial) = 4326),
	CONSTRAINT fk_utility FOREIGN KEY (utility_id)
		REFERENCES public.utility (id) MATCH SIMPLE
		ON UPDATE CASCADE ON DELETE CASCADE
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

CREATE TABLE group_community (
    id integer NOT NULL,
    description character varying,
    image bytea,
    CONSTRAINT pk_group_community PRIMARY KEY (id),
    CONSTRAINT fk_group_community_group FOREIGN KEY (id)
        REFERENCES public."group" (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE group_set (
	id integer NOT NULL,
    owner_id int,
    CONSTRAINT pk_group_set PRIMARY KEY (id),
    CONSTRAINT fk_group_set_group FOREIGN KEY (id)
        REFERENCES public."group" (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_group_cluster_group FOREIGN KEY (owner_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- clusters
CREATE SEQUENCE cluster_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE "cluster" (
    id integer NOT NULL DEFAULT nextval('cluster_id_seq'::regclass),
    key uuid,
    row_version  bigint default 1,
    utility_id integer,
    name character varying(100),
    created_on timestamp without time zone,
    CONSTRAINT pk_cluster PRIMARY KEY (id),
    CONSTRAINT uq_cluster_name_utility UNIQUE (utility_id, name),
	CONSTRAINT fk_cluster_utility FOREIGN KEY (utility_id)
		REFERENCES public.utility (id) MATCH SIMPLE
		ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE group_cluster (
	id integer NOT NULL,
    cluster_id int,
    CONSTRAINT pk_group_cluster PRIMARY KEY (id),
    CONSTRAINT fk_group_cluster_group FOREIGN KEY (id)
        REFERENCES public."group" (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_group_cluster_cluster FOREIGN KEY (cluster_id)
		REFERENCES public."cluster" (id) MATCH SIMPLE
			ON UPDATE CASCADE ON DELETE CASCADE
);

-- favourite
CREATE SEQUENCE favourite_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE favourite (
    id integer NOT NULL DEFAULT nextval('favourite_id_seq'::regclass),
    key uuid,
    row_version  bigint default 1,
    owner_id integer,
    label character varying(100),
    created_on timestamp without time zone,
    CONSTRAINT pk_favourite PRIMARY KEY (id),
	CONSTRAINT fk_favourite_account FOREIGN KEY (owner_id)
		REFERENCES public.account (id) MATCH SIMPLE
			ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE public.favourite_account
(
  id integer NOT NULL,
  account_id int,
  CONSTRAINT pk_favourite_account PRIMARY KEY (id),
  CONSTRAINT fk_favourite_account_favourite FOREIGN KEY (id)
        REFERENCES public.favourite (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_favourite_account_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE public.favourite_group
(
  id integer NOT NULL,
  group_id int,
  CONSTRAINT pk_favourite_group PRIMARY KEY (id),
  CONSTRAINT fk_favourite_group_favourite FOREIGN KEY (id)
        REFERENCES public.favourite (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_favourite_group_group FOREIGN KEY (group_id)
        REFERENCES public."group" (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- alert
CREATE TABLE alert  (
    id integer NOT NULL,
    mode character varying(10),
    priority integer,
    CONSTRAINT pk_alert PRIMARY KEY (id),
	CONSTRAINT ck_mode CHECK (mode::text = 'SWM'::text OR mode::text = 'AMPHIRO'::text OR mode::text = 'BOTH'::text)
);

CREATE SEQUENCE alert_translation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE alert_translation  (
    id integer NOT NULL DEFAULT nextval('alert_translation_id_seq'::regclass),
    alert_id integer,
    locale character(2),
    title character varying(100),
    description character varying,
    link character varying(200),
    CONSTRAINT pk_alert_translation PRIMARY KEY (id),
    CONSTRAINT fk_alert_translation_alert FOREIGN KEY (alert_id)
        REFERENCES public.alert (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE account_alert_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE account_alert (
    id integer NOT NULL DEFAULT nextval('account_alert_id_seq'::regclass),
    account_id integer,
    alert_id integer,
	created_on timestamp without time zone,
	acknowledged_on timestamp without time zone,
    CONSTRAINT pk_account_alert PRIMARY KEY (id),
    CONSTRAINT fk_account_alert_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_account_alert_alert FOREIGN KEY (alert_id)
        REFERENCES public.alert (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE account_alert_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE account_alert_property (
    id integer NOT NULL DEFAULT nextval('account_alert_property_id_seq'::regclass),
    account_alert_id integer,
    key character varying(50),
    value character varying,
    CONSTRAINT pk_account_alert_property PRIMARY KEY (id),
    CONSTRAINT fk_account_alert_property_account_alert FOREIGN KEY (account_alert_id)
        REFERENCES public.account_alert (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- channel
CREATE TABLE channel  (
    id integer NOT NULL,
    name character varying(20) NOT NULL,
    CONSTRAINT pk_channel PRIMARY KEY (id)
);

-- announcement
CREATE TABLE announcement  (
    id integer NOT NULL,
    priority integer NOT NULL,
    CONSTRAINT pk_announcement PRIMARY KEY (id)
);

CREATE TABLE announcement_channel (
    announcement_id integer NOT NULL,
    channel_id integer NOT NULL,
    CONSTRAINT pk_announcement_channel PRIMARY KEY (announcement_id, channel_id),
    CONSTRAINT fk_announcement_channel_announcement FOREIGN KEY (announcement_id)
        REFERENCES public.announcement (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_announcement_channel_channel FOREIGN KEY (channel_id)
        REFERENCES public.channel (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE announcement_translation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE announcement_translation  (
    id integer NOT NULL DEFAULT nextval('announcement_translation_id_seq'::regclass),
    announcement_id integer,
    locale character(2),
    title character varying(100),
    content character varying,
    link character varying(200),
    CONSTRAINT pk_announcement_translation PRIMARY KEY (id),
    CONSTRAINT fk_announcement_translation_announcement FOREIGN KEY (announcement_id)
        REFERENCES public.announcement (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE account_announcement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE account_announcement (
    id integer NOT NULL DEFAULT nextval('account_announcement_id_seq'::regclass),
    account_id integer,
    announcement_id integer,
	created_on timestamp without time zone,
	acknowledged_on timestamp without time zone,
    CONSTRAINT pk_account_announcement PRIMARY KEY (id),
    CONSTRAINT fk_account_announcement_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_account_announcement_announcement FOREIGN KEY (announcement_id)
        REFERENCES public.announcement (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- static recommendation category
CREATE TABLE static_recommendation_category (
    id integer NOT NULL,
    title character varying(100),
    CONSTRAINT pk_static_recommendation_category PRIMARY KEY (id)
);

CREATE SEQUENCE static_recommendation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE static_recommendation  (
    id integer NOT NULL DEFAULT nextval('static_recommendation_id_seq'::regclass),
    index integer NOT NULL,
    category_id integer NOT NULL,
    locale character(2),
    title character varying,
    description character varying,
    image_binary bytea,
    image_link character varying(200),
    prompt character varying(200),
    externa_link character varying(200),
    created_on timestamp without time zone,
    modified_on timestamp without time zone,
    active boolean DEFAULT true NOT NULL,
    source character varying(200),
    CONSTRAINT pk_static_recommendation PRIMARY KEY (id),
    CONSTRAINT fk_static_recommendation_static_recommendation_category FOREIGN KEY (category_id)
        REFERENCES public.static_recommendation_category (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- dynamic recommendation
CREATE TABLE dynamic_recommendation  (
    id integer NOT NULL,
    mode character varying(10),
    priority integer,
    CONSTRAINT pk_dynamic_recommendation PRIMARY KEY (id),
    CONSTRAINT ck_dynamic_recommendation_mode CHECK (mode::text = 'SWM'::text OR mode::text = 'AMPHIRO'::text OR mode::text = 'BOTH'::text)
);

CREATE SEQUENCE dynamic_recommendation_translation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE dynamic_recommendation_translation  (
    id integer NOT NULL DEFAULT nextval('dynamic_recommendation_translation_id_seq'::regclass),
    dynamic_recommendation_id integer NOT NULL,
    locale character(2),
    title character varying(100),
    description character varying,
    image_link character varying(200),
    CONSTRAINT pk_dynamic_recommendation_translation PRIMARY KEY (id),
    CONSTRAINT fk_dynamic_recommendation_translation_dynamic_recommendation FOREIGN KEY (dynamic_recommendation_id)
        REFERENCES public.dynamic_recommendation (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE account_dynamic_recommendation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE account_dynamic_recommendation (
    id integer NOT NULL DEFAULT nextval('account_dynamic_recommendation_id_seq'::regclass),
    account_id integer,
    dynamic_recommendation_id integer,
	created_on timestamp without time zone,
	acknowledged_on timestamp without time zone,
    CONSTRAINT pk_account_dynamic_recommendation PRIMARY KEY (id),
    CONSTRAINT fk_account_dynamic_recommendation_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_account_dynamic_recommendation_dynamic_recommendation FOREIGN KEY (dynamic_recommendation_id)
        REFERENCES public.dynamic_recommendation (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE SEQUENCE account_dynamic_recommendation_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE account_dynamic_recommendation_property (
    id integer NOT NULL DEFAULT nextval('account_dynamic_recommendation_property_id_seq'::regclass),
    account_dynamic_recommendation_id integer,
    key character varying(50),
    value character varying,
    CONSTRAINT pk_account_dynamic_recommendation_property PRIMARY KEY (id),
    CONSTRAINT fk_account_dynamic_recommendation_property FOREIGN KEY (account_dynamic_recommendation_id)
        REFERENCES public.account_dynamic_recommendation (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- Helper views
CREATE OR REPLACE VIEW public.trial_account_activity AS
 SELECT w.id,
    u.id AS utility_id,
    u.name AS utility_name,
    w.account_id,
    a.key,
    w.username,
    w.firstname,
    w.lastname,
    w.registered_on AS signup_date,
    a.last_login_success,
    a.last_login_failure,
    count(da.id) AS amphiro_count,
    count(dm.id) AS meter_count,
    max(case when not da.id is null then d.registered_on else null end) AS amphiro_last_registration,
    max(case when not dm.id is null then d.registered_on else null end) AS meter_last_registration,
    max(d.last_upload_success_on) AS device_last_upload_success,
    max(d.last_upload_failure_on) AS device_last_upload_failure,
    sum(case when not da.id is null then d.transmission_count else null end) AS transmission_count,
    sum(case when not da.id is null then d.transmission_interval_sum else null end) AS transmission_interval_sum,
    max(case when not da.id is null then d.transmission_interval_max else null end) AS transmission_interval_max
   FROM account_white_list w
     JOIN utility u ON w.utility_id = u.id
     LEFT JOIN account a ON w.account_id = a.id
     LEFT JOIN device d ON a.id = d.account_id
     LEFT JOIN device_amphiro da ON da.id = d.id
     LEFT JOIN device_meter dm ON dm.id = d.id
  GROUP BY w.id, u.id, u.name, w.account_id, a.key, w.username, w.firstname, w.lastname, w.registered_on, a.last_login_success, a.last_login_failure;

ALTER TABLE public.trial_account_activity
  OWNER TO daiad;

-- procedures
CREATE FUNCTION sp_account_update_stats(account_id integer, success boolean, login_date timestamp with time zone) RETURNS INT AS $$
BEGIN
	if  success = true then
		update account set last_login_success = GREATEST(login_date AT TIME ZONE 'UTC', last_login_success) where id = account_id;
	else
		update account set last_login_failure = GREATEST(login_date AT TIME ZONE 'UTC', last_login_failure) where id = account_id;
	end if;

	return 1;
END;
$$ LANGUAGE plpgsql;
