
CREATE SEQUENCE public.utility_statistics_id_seq INCREMENT 1 MINVALUE 1 START 1;

CREATE TABLE public.utility_statistics (
  id int4 NOT NULL DEFAULT nextval('utility_statistics_id_seq'::regclass),
  ref_date timestamp without time zone NOT NULL,
  period character varying(32) NOT NULL,
  utility integer NOT NULL,
  "group" integer,
  statistic character varying(127) NOT NULL,
  field character varying(127) NOT NULL,
  device_type character varying(127) NOT NULL,
  value double precision NOT NULL,
  computed_at timestamp without time zone,
  CONSTRAINT utility_statistics_pkey PRIMARY KEY (id),
  CONSTRAINT utility_statistics_group_fkey FOREIGN KEY ("group")
      REFERENCES public."group" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT utility_statistics_utility_fkey FOREIGN KEY (utility)
      REFERENCES public.utility (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE 
);

CREATE INDEX idx_utility_statistics_1 ON public.utility_statistics
  USING btree(utility, ref_date);

CREATE UNIQUE INDEX idx_utility_statistics_2 ON public.utility_statistics
  USING btree (utility, (COALESCE("group", (-1))), ref_date, period, statistic, field, device_type);


