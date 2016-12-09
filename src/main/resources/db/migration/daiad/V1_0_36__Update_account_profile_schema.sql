-- Add field for DAIAD@home mobile client version
alter table public.account_profile add mobile_app_version character varying(14) null;
