-- Drop table public.water_iq;
DROP TABLE IF EXISTS public.water_iq;

-- Add fields for year and month
alter table public.water_iq_history add interval_year int null;

alter table public.water_iq_history add interval_month int null;

-- Update existing data
update public.water_iq_history set interval_year = substring(interval_from from 1 for 4)::int;

update public.water_iq_history set interval_month = substring(interval_from from 5 for 2)::int;

-- Make new fields not nullable
alter table public.water_iq_history alter column interval_year set not null;

alter table public.water_iq_history alter column interval_month set not null;
