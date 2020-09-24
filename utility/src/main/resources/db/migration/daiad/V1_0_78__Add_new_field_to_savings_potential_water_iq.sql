delete from savings_potential_water_iq;

ALTER TABLE savings_potential_water_iq ADD COLUMN "cluster" character varying(50) not null;
