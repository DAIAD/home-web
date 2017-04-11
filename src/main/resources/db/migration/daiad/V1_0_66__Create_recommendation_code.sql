CREATE TABLE public.recommendation_code (
    code character varying(31) PRIMARY KEY,
    "type" int4 NOT NULL REFERENCES public.recommendation_type(value)
);

CREATE VIEW public.recommendation_type_view AS 
    SELECT 
      t.*, (SELECT array_agg(c.code) FROM public.recommendation_code c WHERE c."type" = t.value) AS codes
    FROM public.recommendation_type t;
