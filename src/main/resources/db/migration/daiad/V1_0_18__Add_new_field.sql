-- Add new field [application] to table [password_reset_token]
alter table password_reset_token add application character varying(15);
