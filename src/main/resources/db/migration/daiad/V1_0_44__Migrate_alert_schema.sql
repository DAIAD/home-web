
-- Drop retired tables/sequences

DROP VIEW alert_analytics;

DROP SEQUENCE public.account_alert_property_id_seq CASCADE;
DROP TABLE public.account_alert_property;

DROP SEQUENCE public.account_alert_id_seq CASCADE;
DROP TABLE public.account_alert;

DROP SEQUENCE public.alert_translation_id_seq CASCADE;
DROP TABLE public.alert_translation;

DROP TABLE public.alert;

--
-- Table public.public.alert_type
--

CREATE TABLE public.alert_type
(
    value integer NOT NULL,
    priority integer NOT NULL,
    name character varying(255) NOT NULL,
    CONSTRAINT alert_type_pkey PRIMARY KEY (value),
    CONSTRAINT uq_alert_type_name UNIQUE (name)
);

CREATE OR REPLACE FUNCTION public.alert_type_from_name(text) RETURNS integer AS $$
    SELECT rt.value FROM alert_type AS rt WHERE rt.name = $1 
$$ LANGUAGE SQL;

--
-- Table public.public.alert_template
--

CREATE TABLE public.alert_template
(
    value integer NOT NULL,
    name character varying(255) NOT NULL,
    "type" integer NOT NULL,
    CONSTRAINT alert_template_pkey PRIMARY KEY (value),
    CONSTRAINT fk_alert_template_type FOREIGN KEY ("type")
        REFERENCES public.alert_type (value) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT uq_alert_template_name UNIQUE (name)
);

CREATE OR REPLACE FUNCTION public.alert_template_from_name(text) RETURNS integer AS $$
    SELECT rt.value FROM alert_template AS rt WHERE rt.name = $1 
$$ LANGUAGE SQL;


--
-- Table public.public.alert_template_translation
--

CREATE SEQUENCE public.alert_template_translation_id_seq INCREMENT 1 MINVALUE 1;

CREATE TABLE public.alert_template_translation
(
    id integer NOT NULL DEFAULT nextval('alert_template_translation_id_seq'::regclass),
    template integer NOT NULL,
    locale bpchar NOT NULL,
    description character varying,
    title character varying(255) NOT NULL,
    link character varying(255),
    CONSTRAINT alert_template_translation_pkey PRIMARY KEY (id),
    CONSTRAINT fk_alert_template_translation_template FOREIGN KEY (template)
        REFERENCES public.alert_template (value) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT uq_alert_template_translation_1 UNIQUE (template, locale)
);

--
-- Table public.public.account_alert
--

CREATE SEQUENCE public.account_alert_id_seq INCREMENT 1 MINVALUE 1;

CREATE TABLE public.account_alert
(
    id integer NOT NULL DEFAULT nextval('account_alert_id_seq'::regclass),
    acknowledged_on timestamp without time zone,
    created_on timestamp without time zone,
    receive_acknowledged_on timestamp without time zone,
    account_id integer NOT NULL,
    alert_template integer NOT NULL,
    CONSTRAINT account_alert_pkey PRIMARY KEY (id),
    CONSTRAINT fk_account_alert_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT fk_account_alert_template FOREIGN KEY (alert_template)
        REFERENCES public.alert_template (value) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE
);

--
-- Table public.public.account_alert_parameter
--

CREATE SEQUENCE public.account_alert_parameter_id_seq INCREMENT 1 MINVALUE 1;

CREATE TABLE public.account_alert_parameter
(
    id integer NOT NULL DEFAULT nextval('account_alert_parameter_id_seq'::regclass),
    key character varying(255),
    value character varying(255),
    account_alert_id integer NOT NULL,
    CONSTRAINT account_alert_parameter_pkey PRIMARY KEY (id),
    CONSTRAINT fk_account_alert_parameter_1 FOREIGN KEY (account_alert_id)
        REFERENCES public.account_alert (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE
);

--
-- View alert_analytics
--

CREATE VIEW alert_analytics AS 
SELECT at.name AS "type", count(1) AS "count"
FROM 
    account_alert aa 
    LEFT JOIN alert_template t ON (aa.alert_template = t.value)
    LEFT JOIN alert_type at ON (at.value = t."type")
GROUP BY at.name 

