
CREATE OR REPLACE VIEW public.alert_analytics AS
select	al.id, at.title, at.description, at.locale, count(1) as total
from	alert al
	  left outer join alert_translation at
	    on al.id = at.alert_id
	  left outer join account_alert aa
	    on aa.alert_id = al.id
	  left outer join account ac
	    on aa.account_id = ac.id
group by al.id, at.title, at.description, at.locale;

CREATE OR REPLACE VIEW public.recommendation_analytics AS
select	r.id, rt.title, rt.description, rt.locale, count(1) as total
from	dynamic_recommendation r
	  left outer join dynamic_recommendation_translation rt
	    on r.id = rt.dynamic_recommendation_id
	  left outer join account_dynamic_recommendation ar
	    on ar.dynamic_recommendation_id = r.id
	  left outer join account ac
	    on ar.account_id = ac.id
group by r.id, rt.title, rt.description, rt.locale;
