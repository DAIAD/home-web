CREATE SEQUENCE area_group_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE area_group (
    id integer NOT NULL DEFAULT nextval('area_group_id'::regclass),
    utility_id integer not null,
	key uuid,
    row_version bigint default 1,
	title character varying(100),
    created_on timestamp without time zone,
	bbox geometry,
	level_count int not null default 1,
    CONSTRAINT pk_area_group PRIMARY KEY (id),
    CONSTRAINT fk_utility FOREIGN KEY (utility_id)
        REFERENCES public.utility (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT enforce_dims_bbox CHECK (st_ndims(bbox) = 2),
    CONSTRAINT enforce_srid_bbox CHECK (st_srid(bbox) = 4326)
);

CREATE SEQUENCE area_group_item_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE area_group_item (
    id integer NOT NULL DEFAULT nextval('area_group_item_id'::regclass),
    utility_id integer not null,
    area_group_id integer null,
	key uuid,
    row_version bigint default 1,
	title character varying(100),
    created_on timestamp without time zone,
	the_geom geometry not null,
	level_index int not null,
    CONSTRAINT pk_area_group_item PRIMARY KEY (id),
    CONSTRAINT fk_utility FOREIGN KEY (utility_id)
        REFERENCES public.utility (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_area_group_item_id_area_group FOREIGN KEY (area_group_id)
        REFERENCES public.area_group (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
    CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
);

CREATE SEQUENCE area_user_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE area_user (
    id integer NOT NULL DEFAULT nextval('area_user_id'::regclass),
    utility_id integer not null,
    account_id integer null,
	key uuid,
    row_version bigint default 1,
	title character varying(100),
    created_on timestamp without time zone,
	the_geom geometry,
    CONSTRAINT pk_area_user PRIMARY KEY (id),
    CONSTRAINT fk_utility FOREIGN KEY (utility_id)
        REFERENCES public.utility (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_area_user_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
    CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
);

