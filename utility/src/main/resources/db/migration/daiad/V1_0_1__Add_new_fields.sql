-- Add new fields
alter table account_alert add receive_acknowledged_on timestamp without time zone;

alter table account_dynamic_recommendation add receive_acknowledged_on timestamp without time zone;
