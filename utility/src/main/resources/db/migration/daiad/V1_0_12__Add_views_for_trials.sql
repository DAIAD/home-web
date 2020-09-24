CREATE OR REPLACE VIEW public.trial_account_device_amphiro AS
select 	a.id as account_id, a.username, d.id as device_id,
	d.registered_on, d.last_upload_success_on, d.last_upload_failure_on,
	da.name, c.id as config_id, c.version, c.title, c.created_on, c.enabled_on, c.acknowledged_on
from	account a
	  inner join device d
	    on a.id = d.account_id
	  inner join device_amphiro da
	    on d.id = da.id
	  inner join device_amphiro_config c
	    on d.id = c.id
where	c.active = true;

CREATE OR REPLACE VIEW public.trial_account_device_meter AS
select 	a.id as account_id, a.username, d.id as device_id,
	d.registered_on, d.last_upload_success_on, d.last_upload_failure_on,
	m.serial
from	account a
	  inner join device d
	    on a.id = d.account_id
	  inner join device_meter m
	    on d.id = m.id;

CREATE OR REPLACE VIEW public.trial_account_profile AS
select 	a.id as account_id, a.username,
	p.version, p.updated_on, p.web_mode, p.mobile_mode, p.utility_mode,
	case
            when p.web_mode = 1 then 'Enabled'
            when p.web_mode = 2 then 'Disabled'
            else 'Unknown'
        end as web_mode_text,
        case
            when p.mobile_mode = 1 then 'Enabled'
            when p.mobile_mode = 2 then 'Disabled'
            when p.mobile_mode = 3 then 'Learning'
            when p.mobile_mode = 4 then 'Blocked'
            else 'Unknown'
        end as mobile_mode_text,
        case
            when p.utility_mode = 1 then 'Enabled'
            when p.utility_mode = 2 then 'Disabled'
            else 'Unknown'
        end as utility_mode_text,
        h.enabled_on, h.acknowledged_on,
        s.table_os as tablet_os,
        s.smart_phone_os
from	account a
	  inner join account_profile p
	    on a.id = p.id
	  inner join account_profile_history h
	    on p.id = h.profile_id and p.version = h.version
	  left outer join survey s
	    on a.username = s.username;
