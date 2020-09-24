
DROP SEQUENCE IF EXISTS public.account_recommendation_parameters_id_seq CASCADE;

DROP TABLE IF EXISTS public.account_recommendation_parameters CASCADE;


CREATE SEQUENCE public.account_recommendation_parameters_id_seq MINVALUE 1 START 1 INCREMENT 1;

CREATE TABLE public.account_recommendation_parameters (
    id integer PRIMARY KEY DEFAULT nextval('account_recommendation_parameters_id_seq'::regclass),
    account_recommendation_id int4 not null,
    class_name character varying(256) not null,
    json_data character varying not null 
 );

ALTER TABLE public.account_recommendation_parameters 
    ADD CONSTRAINT fk_account_recommendation_parameters FOREIGN KEY (account_recommendation_id) REFERENCES public.account_recommendation(id);


-- Drop obsolete tables/sequences

DROP SEQUENCE IF EXISTS public.account_recommendation_parameter_id_seq CASCADE;

DROP TABLE IF EXISTS public.account_recommendation_parameter CASCADE;


