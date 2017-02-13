-- Add field to favourite queries for pinning to dashboard
alter table public.data_query add pinned boolean NOT NULL default (false);
