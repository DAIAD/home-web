CREATE TABLE IF NOT EXISTS survey (
	username character varying(100) NOT NULL,
	firstname character varying(40) NOT NULL,
	lastname character varying(70) NOT NULL,
	address character varying(90) NOT NULL,
	city character varying(40) NOT NULL,
	gender character varying(12) NOT NULL,
	number_of_showers integer NOT NULL,
	smart_phone_os character varying(20) NOT NULL,
	table_os character varying(20) NOT NULL,
	apartment_size_bracket character varying(40) NOT NULL,
	age integer NULL,
	household_member_total integer NULL,
	household_member_female integer NULL,
	household_member_male integer NULL,
	income_bracket character varying(50) NULL,
	meter_id character varying(12) NULL,
	utility_id integer NOT NULL,
	CONSTRAINT pk_survey PRIMARY KEY (username),
	CONSTRAINT fk_utility_survey FOREIGN KEY (utility_id)
		REFERENCES public.utility (id) MATCH SIMPLE
			ON UPDATE CASCADE ON DELETE CASCADE
);
