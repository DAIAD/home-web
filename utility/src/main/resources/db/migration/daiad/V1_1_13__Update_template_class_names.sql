update account_alert_parameters
set class_name = 'eu.daiad.common' || substring(class_name, 13)
where class_name like 'eu.daiad.web.%';

update account_recommendation_parameters
set class_name = 'eu.daiad.common' || substring(class_name, 13)
where class_name like 'eu.daiad.web.%';
