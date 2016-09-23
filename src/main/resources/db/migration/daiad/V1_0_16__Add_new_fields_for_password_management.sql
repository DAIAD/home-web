-- Add new field [allow_password_reset] to table [account]
alter table account add allow_password_reset boolean NOT NULL DEFAULT false;

-- Add new field [allow_password_reset] to table [account]
alter table password_reset_token add pin character varying(20);
