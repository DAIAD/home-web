-- Add new fields [social_enabled], [garden] and [unit] to table [account_profile]
alter table account_profile add social_enabled boolean not null default false;

alter table account_profile add unit character varying(12) null;

alter table account_profile add garden boolean null;

-- Add new field [social_enabled] to table [account_profile_history]
alter table account_profile_history add social_enabled boolean not null default false;
