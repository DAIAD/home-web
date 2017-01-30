
ALTER TABLE public.static_recommendation_category RENAME TO tip_category;


ALTER SEQUENCE public.static_recommendation_id_seq RENAME TO tip_id_seq;

ALTER TABLE public.static_recommendation RENAME TO tip;

ALTER TABLE public.tip RENAME CONSTRAINT pk_static_recommendation TO pk_tip;
ALTER TABLE public.tip RENAME CONSTRAINT fk_static_recommendation_static_recommendation_category TO fk_tip_tip_category;

ALTER table public.tip DROP CONSTRAINT IF EXISTS uq_tip_1;
ALTER table public.tip ADD CONSTRAINT uq_tip_1 unique ("index", locale);


ALTER SEQUENCE public.account_static_recommendation_id_seq RENAME TO account_tip_id_seq;

ALTER TABLE public.account_static_recommendation RENAME TO account_tip;
ALTER TABLE public.account_tip RENAME static_recommendation_id TO tip_id;


