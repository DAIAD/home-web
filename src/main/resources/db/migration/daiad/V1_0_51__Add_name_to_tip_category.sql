ALTER TABLE public.tip_category 
    ADD COLUMN "name" character varying(100);

UPDATE public.tip_category 
    SET "name" = regexp_replace(lower(title), '[ &]+', '-', 'g');

ALTER TABLE public.tip_category 
    ADD CONSTRAINT uq_tip_category_name UNIQUE ("name");
