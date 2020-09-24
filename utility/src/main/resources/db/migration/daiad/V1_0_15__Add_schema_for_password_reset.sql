DROP TABLE IF EXISTS public.password_reset_token;
DROP SEQUENCE IF EXISTS public.password_reset_token_id_seq;

CREATE SEQUENCE public.password_reset_token_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE public.password_reset_token
(
  id int NOT NULL DEFAULT nextval('password_reset_token_id_seq'::regclass),
  account_id int NOT NULL,
  created_on timestamp without time zone,
  redeemed_on timestamp without time zone,
  token uuid,
  valid boolean NOT NULL,
  CONSTRAINT pk_password_reset_token PRIMARY KEY (id),
  CONSTRAINT fk_password_reset_token_account FOREIGN KEY (account_id)
	REFERENCES public.account (id) MATCH SIMPLE
	  ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
