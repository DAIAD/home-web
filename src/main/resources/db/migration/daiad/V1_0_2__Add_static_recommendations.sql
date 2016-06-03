CREATE SEQUENCE account_static_recommendation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
CREATE TABLE public.account_static_recommendation
(
  id integer NOT NULL DEFAULT nextval('account_static_recommendation_id_seq'::regclass),
  account_id integer NOT NULL,
  static_recommendation_id integer NOT NULL,
  created_on timestamp without time zone NOT NULL,
  acknowledged_on timestamp without time zone,
  receive_acknowledged_on timestamp without time zone,
  CONSTRAINT pk_account_static_recommendation PRIMARY KEY (id),
  CONSTRAINT fk_account_static_recommendation_account FOREIGN KEY (account_id)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_account_static_recommendation_static_recommendation FOREIGN KEY (static_recommendation_id)
      REFERENCES public.static_recommendation (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
