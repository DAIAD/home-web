
CREATE SEQUENCE public.alert_resolver_execution_id_seq START 1 INCREMENT 1;

CREATE TABLE public.alert_resolver_execution (
    id int4 PRIMARY KEY DEFAULT nextval('alert_resolver_execution_id_seq'::regclass),
    ref_date timestamp not null,
    resolver_name varchar(255) NOT NULL,
    started timestamp NOT NULL,
    finished timestamp,
    target int4 NULL REFERENCES public.utility(id),
    target_group int4 NULL REFERENCES public."group"(id)
 );



CREATE SEQUENCE public.recommendation_resolver_execution_id_seq START 1 INCREMENT 1;

CREATE TABLE public.recommendation_resolver_execution (
    id int4 PRIMARY KEY DEFAULT nextval('recommendation_resolver_execution_id_seq'::regclass),
    ref_date timestamp not null,
    resolver_name varchar(255) NOT NULL,
    started timestamp NOT NULL,
    finished timestamp,
    target int4 NULL REFERENCES public.utility(id),
    target_group int4 NULL REFERENCES public."group"(id)
 );



TRUNCATE TABLE public.account_alert CASCADE;

ALTER TABLE public.account_alert
   ADD COLUMN device_type character varying(16) NOT NULL CHECK (device_type = 'METER' OR device_type = 'AMPHIRO');

ALTER TABLE public.account_alert
   ADD COLUMN resolver_execution int4 NOT NULL REFERENCES public.alert_resolver_execution(id);



TRUNCATE TABLE public.account_recommendation CASCADE;

ALTER TABLE public.account_recommendation
   ADD COLUMN device_type character varying(16) NOT NULL CHECK (device_type = 'METER' OR device_type = 'AMPHIRO');

ALTER TABLE public.account_recommendation
   ADD COLUMN resolver_execution int4 NOT NULL REFERENCES public.recommendation_resolver_execution(id);


