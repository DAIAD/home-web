DROP TABLE IF EXISTS public.weather_data_hour;
DROP SEQUENCE IF EXISTS public.weather_data_hour_id_seq;

DROP TABLE IF EXISTS public.weather_data_day;
DROP SEQUENCE IF EXISTS public.weather_data_day_id_seq;

DROP TABLE IF EXISTS public.weather_service_utility_parameter;
DROP SEQUENCE IF EXISTS public.weather_service_utility_parameter_id_seq;

DROP TABLE IF EXISTS public.weather_service_utility;
DROP SEQUENCE IF EXISTS public.weather_service_utility_id_seq;

DROP TABLE IF EXISTS public.weather_service;

CREATE TABLE public.weather_service
(
  id int NOT NULL,
  name character varying(100),
  description character varying,
  endpoint character varying,
  website character varying,
  registered_on timestamp without time zone,
  bean character varying,
  active boolean NOT NULL,
  CONSTRAINT pk_weather_service PRIMARY KEY (id),
  CONSTRAINT uq_weather_service_name UNIQUE (name)
)
WITH (
  OIDS=FALSE
);

CREATE SEQUENCE public.weather_service_utility_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.weather_service_utility
(
  id int NOT NULL DEFAULT nextval('weather_service_utility_id_seq'::regclass),
  service_id int NOT NULL,
  utility_id int NOT NULL,
  CONSTRAINT pk_weather_service_utility PRIMARY KEY (id),
  CONSTRAINT fk_service FOREIGN KEY (service_id)
	REFERENCES public.weather_service (id) MATCH SIMPLE
	  ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_utility FOREIGN KEY (utility_id)
	REFERENCES public.utility (id) MATCH SIMPLE
	  ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

CREATE SEQUENCE public.weather_service_utility_parameter_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.weather_service_utility_parameter
(
  id int NOT NULL DEFAULT nextval('weather_service_utility_parameter_id_seq'::regclass),
  service_utility_id int NOT NULL,
  key character varying(50),
  value character varying,
  CONSTRAINT pk_weather_service_utility_parameter PRIMARY KEY (id),
  CONSTRAINT fk_weather_service_utility FOREIGN KEY (service_utility_id)
	REFERENCES public.weather_service_utility (id) MATCH SIMPLE
	  ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

CREATE SEQUENCE public.weather_data_day_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.weather_data_day
(
  id bigint NOT NULL DEFAULT nextval('weather_data_day_id_seq'::regclass),
  utility_id int NOT NULL,
  service_id int NOT NULL,
  min_temperature  float,
  max_temperature  float,
  min_temperature_feel  float,
  max_temperature_feel  float,
  min_humidity float,
  max_humidity float,
  precipitation float,
  wind_speed float,
  wind_direction character varying,
  conditions character varying,
  created_on  timestamp without time zone,
  date character(8),
  CONSTRAINT pk_weather_data_day PRIMARY KEY (id),
  CONSTRAINT fk_weather_data_utility FOREIGN KEY (utility_id)
	REFERENCES public.utility (id) MATCH SIMPLE
	  ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_weather_data_service FOREIGN KEY (service_id)
	REFERENCES public.weather_service (id) MATCH SIMPLE
	  ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

CREATE INDEX idx_weather_data_day_date
  ON public.weather_data_day USING btree (date);

CREATE SEQUENCE public.weather_data_hour_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.weather_data_hour
(
  id bigint NOT NULL DEFAULT nextval('weather_data_hour_id_seq'::regclass),
  day_id bigint NOT NULL,
  temperature  float,
  temperature_feel  float,
  humidity float,
  precipitation float,
  wind_speed float,
  wind_direction character varying,
  conditions character varying,
  datetime character(10),
  CONSTRAINT pk_weather_data_hour PRIMARY KEY (id),
  CONSTRAINT fk_weather_data_hour_day FOREIGN KEY (day_id)
	REFERENCES public.weather_data_day (id) MATCH SIMPLE
	  ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

CREATE INDEX idx_weather_data_hour_datetime
  ON public.weather_data_hour USING btree (datetime);
