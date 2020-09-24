-- Drop existing objects
DROP TABLE IF EXISTS public.daily_counter;

DROP SEQUENCE IF EXISTS public.daily_counter_id_seq;

-- Create new objects
CREATE SEQUENCE daily_counter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE daily_counter (
    id bigint DEFAULT nextval('daily_counter_id_seq'::regclass) not null,
    utility_id int not null,
    date_created timestamp without time zone not null,
    counter_name character varying not null,
    counter_value bigint not null,   
    CONSTRAINT pk_daily_counter PRIMARY KEY (id)
);
        
