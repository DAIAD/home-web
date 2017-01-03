
DROP SEQUENCE public.account_dynamic_recommendation_property_id_seq;
DROP TABLE public.account_dynamic_recommendation_property;

DROP SEQUENCE public.account_dynamic_recommendation_id_seq;
DROP TABLE public.account_dynamic_recommendation;

DROP SEQUENCE public.dynamic_recommendation_translation_id_seq;
DROP TABLE public.dynamic_recommendation_translation;

DROP TABLE public.dynamic_recommendation;

--
-- Table public.public.recommendation_type
--

CREATE TABLE public.recommendation_type
(
    value integer NOT NULL,
    priority integer NOT NULL,
    name character varying(255) NOT NULL,
    CONSTRAINT recommendation_type_pkey PRIMARY KEY (value),
    CONSTRAINT uq_recommendation_type_name UNIQUE (name)
);

CREATE OR REPLACE FUNCTION public.recommendation_type_from_name(text) RETURNS integer AS $$
    SELECT rt.value FROM recommendation_type AS rt WHERE rt.name = $1 
$$ LANGUAGE SQL;

--
-- Table public.public.recommendation_template
--

CREATE TABLE public.recommendation_template
(
    value integer NOT NULL,
    name character varying(255) NOT NULL,
    "type" integer NOT NULL,
    CONSTRAINT recommendation_template_pkey PRIMARY KEY (value),
    CONSTRAINT fk_recommendation_template_type FOREIGN KEY ("type")
        REFERENCES public.recommendation_type (value) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT uq_recommendation_template_name UNIQUE (name)
);

CREATE OR REPLACE FUNCTION public.recommendation_template_from_name(text) RETURNS integer AS $$
    SELECT rt.value FROM recommendation_template AS rt WHERE rt.name = $1 
$$ LANGUAGE SQL;


--
-- Table public.public.recommendation_template_translation
--

CREATE SEQUENCE public.recommendation_template_translation_id_seq INCREMENT 1 MINVALUE 1;

CREATE TABLE public.recommendation_template_translation
(
    id integer NOT NULL DEFAULT nextval('recommendation_template_translation_id_seq'::regclass),
    template integer NOT NULL,
    locale bpchar NOT NULL,
    description character varying,
    title character varying(255) NOT NULL,
    image_link character varying(255),
    CONSTRAINT recommendation_template_translation_pkey PRIMARY KEY (id),
    CONSTRAINT fk_recommendation_template_translation_template FOREIGN KEY (template)
        REFERENCES public.recommendation_template (value) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT uq_recommendation_template_translation_1 UNIQUE (template, locale)
);

--
-- Table public.public.account_recommendation
--

CREATE SEQUENCE public.account_recommendation_id_seq INCREMENT 1 MINVALUE 1;

CREATE TABLE public.account_recommendation
(
    id integer NOT NULL DEFAULT nextval('account_recommendation_id_seq'::regclass),
    acknowledged_on timestamp without time zone,
    created_on timestamp without time zone,
    receive_acknowledged_on timestamp without time zone,
    account_id integer NOT NULL,
    recommendation_template integer NOT NULL,
    CONSTRAINT account_recommendation_pkey PRIMARY KEY (id),
    CONSTRAINT fk_account_recommendation_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT fk_account_recommendation_template FOREIGN KEY (recommendation_template)
        REFERENCES public.recommendation_template (value) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE
);

--
-- Table public.public.account_recommendation_parameter
--

CREATE SEQUENCE public.account_recommendation_parameter_id_seq INCREMENT 1 MINVALUE 1;

CREATE TABLE public.account_recommendation_parameter
(
    id integer NOT NULL DEFAULT nextval('account_recommendation_parameter_id_seq'::regclass),
    key character varying(255),
    value character varying(255),
    account_recommendation_id integer NOT NULL,
    CONSTRAINT account_recommendation_parameter_pkey PRIMARY KEY (id),
    CONSTRAINT fk_account_recommendation_parameter_1 FOREIGN KEY (account_recommendation_id)
        REFERENCES public.account_recommendation (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE
);




