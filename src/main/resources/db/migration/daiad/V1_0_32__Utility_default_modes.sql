-- Default amphiro b1 mode (1: Off configuration). See table public.device_amphiro_config_default for amphiro b1 default modes.
alter table public.utility add default_amphiro_mode int NOT NULL default(1);

-- Default mobile mode (3: Learning mode)
alter table public.utility add default_mobile_mode character varying (20) NOT NULL default('LEARNING');

-- Default web mode (2: Inactive)
alter table public.utility add default_web_mode character varying (20) NOT NULL default('INACTIVE');

-- Default social mode (false : Disabled)
alter table public.utility add default_social_mode boolean NOT NULL default(false);
