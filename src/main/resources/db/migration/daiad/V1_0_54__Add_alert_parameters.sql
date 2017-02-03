
DROP SEQUENCE IF EXISTS public.account_alert_parameters_id_seq CASCADE;

DROP TABLE IF EXISTS public.account_alert_parameters CASCADE;


CREATE SEQUENCE public.account_alert_parameters_id_seq MINVALUE 1 START 1 INCREMENT 1;

CREATE TABLE public.account_alert_parameters (
    id integer PRIMARY KEY DEFAULT nextval('account_alert_parameters_id_seq'::regclass),
    account_alert_id int4 not null,
    class_name character varying(256) not null,
    json_data character varying not null 
 );

ALTER TABLE public.account_alert_parameters 
    ADD CONSTRAINT fk_account_alert_parameters FOREIGN KEY (account_alert_id) REFERENCES public.account_alert(id);


-- Drop obsolete tables/sequences

DROP SEQUENCE IF EXISTS public.account_alert_parameter_id_seq CASCADE;

DROP TABLE IF EXISTS public.account_alert_parameter CASCADE;


