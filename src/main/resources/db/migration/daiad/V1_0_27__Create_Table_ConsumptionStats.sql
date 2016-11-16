CREATE SEQUENCE public.consumption_stats_id_seq 
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.consumption_stats
(
  "id" bigint PRIMARY KEY DEFAULT nextval('consumption_stats_id_seq'::regclass),
  "ref_date" timestamp NOT NULL,
  "utility" integer NOT NULL REFERENCES utility ("id") ON DELETE CASCADE,
  "group" integer REFERENCES "group" (id) ON DELETE CASCADE,
  "statistic" character varying(127) NOT NULL,
  "field" character varying(127) NOT NULL,
  "device" character varying(127) NOT NULL,
  "value" double precision NOT NULL,
  "computed_at" timestamp
)
WITH (
  OIDS=FALSE
);

CREATE INDEX idx_consumption_stats_1 
  ON public.consumption_stats (utility, "group", ref_date);

CREATE UNIQUE INDEX idx_consumption_stats_2 
  ON public.consumption_stats (utility, coalesce("group", -1), ref_date, statistic, field, device)
