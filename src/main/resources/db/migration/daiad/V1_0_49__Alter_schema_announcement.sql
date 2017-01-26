
ALTER TABLE public.announcement_translation 
    DROP CONSTRAINT IF EXISTS uq_announcement_translation_1;
ALTER TABLE public.announcement_translation 
    ADD CONSTRAINT uq_announcement_translation_1 UNIQUE (announcement_id, locale);

ALTER TABLE public.announcement_translation
    DROP COLUMN IF EXISTS dispatched_on;



ALTER TABLE public.account_announcement 
    DROP CONSTRAINT IF EXISTS  uq_account_announcement_1;
ALTER TABLE public.account_announcement 
    ADD CONSTRAINT uq_account_announcement_1 UNIQUE (account_id, announcement_id);


DROP SEQUENCE IF EXISTS public.announcement_channel_id_seq CASCADE;
DROP TABLE IF EXISTS public.announcement_channel;

CREATE SEQUENCE public.announcement_channel_id_seq START 1 INCREMENT 1;

CREATE TABLE public.announcement_channel
(
  id integer PRIMARY KEY DEFAULT nextval('announcement_channel_id_seq'::regclass),
  announcement_id integer NOT NULL,
  channel_id integer NOT NULL,
  CONSTRAINT fk_announcement_channel_announcement FOREIGN KEY (announcement_id)
      REFERENCES public.announcement (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_announcement_channel_channel FOREIGN KEY (channel_id)
      REFERENCES public.channel (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

ALTER TABLE public.announcement_channel
    ADD CONSTRAINT uq_announcement_channel_1 UNIQUE (announcement_id, channel_id);


DROP SEQUENCE IF EXISTS public.announcement_id_seq CASCADE;
CREATE SEQUENCE public.announcement_id_seq START 1 INCREMENT 1;

ALTER TABLE public.announcement 
    ALTER COLUMN id SET DEFAULT nextval('announcement_id_seq'::regclass);

