CREATE TABLE public.alert_code (
    code character varying(31) PRIMARY KEY,
    "type" int4 NOT NULL REFERENCES public.alert_type(value)
);

CREATE VIEW public.alert_type_view AS 
    SELECT 
      t.*, (SELECT array_agg(c.code) FROM public.alert_code c WHERE c."type" = t.value) AS codes
    FROM public.alert_type t;
