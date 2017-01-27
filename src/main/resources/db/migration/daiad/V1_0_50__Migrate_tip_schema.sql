
ALTER TABLE public.static_recommendation_category RENAME TO public.tip_category;

ALTER SEQUENCE public.static_recommendation_id_seq; RENAME TO public.tip_id_seq

ALTER TABLE public.static_recommendation RENAME TO public.tip;

