
ALTER TABLE public.static_recommendation_category RENAME TO public.tip_category;


ALTER SEQUENCE public.static_recommendation_id_seq; RENAME TO public.tip_id_seq

ALTER TABLE public.static_recommendation RENAME TO public.tip;

--ALTER table public.tip DROP CONSTRAINT IF EXISTS uq_tip_1 unique ("index", locale);
ALTER table public.tip ADD CONSTRAINT uq_tip_1 unique ("index", locale);


ALTER SEQUENCE public.account_static_recommendation_id_seq; RENAME TO public.account_tip_id_seq

ALTER TABLE public.account_static_recommendation RENAME TO public.account_tip;


