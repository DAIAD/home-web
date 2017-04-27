DROP TABLE IF EXISTS public.price_bracket;
DROP SEQUENCE IF EXISTS public.price_bracket_id_seq;

CREATE SEQUENCE public.price_bracket_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.price_bracket
(
  id int not null default nextval('price_bracket_id_seq'::regclass),
  utility_id int not null,
  created_on timestamp without time zone not null,
  interval_from character varying(8) not null,
  interval_to character varying(8) not null,
  volume double precision null,
  price double precision not null,
  CONSTRAINT pk_price_bracket PRIMARY KEY (id),
  CONSTRAINT fk_price_bracket_utility FOREIGN KEY (utility_id)
    REFERENCES public.utility (id) MATCH SIMPLE
    ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
