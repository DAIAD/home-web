-- procedures
DROP FUNCTION IF EXISTS sp_account_update_stats(account_id integer, success boolean, login_date timestamp with time zone);

-- views
DROP VIEW IF EXISTS public.trial_account_activity;

-- survey
DROP TABLE IF EXISTS public.survey;

-- alert
DROP TABLE IF EXISTS public.account_alert_property;

DROP SEQUENCE IF EXISTS public.account_alert_property_id_seq;

DROP TABLE IF EXISTS public.account_alert;

DROP SEQUENCE IF EXISTS public.account_alert_id_seq;

DROP TABLE IF EXISTS public.alert_translation;

DROP SEQUENCE IF EXISTS public.alert_translation_id_seq;

DROP TABLE IF EXISTS public.alert;

-- announcement
DROP TABLE IF EXISTS public.account_announcement;

DROP SEQUENCE IF EXISTS public.account_announcement_id_seq;

DROP TABLE IF EXISTS public.announcement_translation;

DROP SEQUENCE IF EXISTS public.announcement_translation_id_seq;

DROP TABLE IF EXISTS public.announcement_channel;

DROP TABLE IF EXISTS public.announcement;

-- channel
DROP TABLE IF EXISTS public.channel;

-- dynamic recommendation
DROP TABLE IF EXISTS public.account_dynamic_recommendation_property;

DROP SEQUENCE IF EXISTS public.account_dynamic_recommendation_property_id_seq;

DROP TABLE IF EXISTS public.account_dynamic_recommendation;

DROP SEQUENCE IF EXISTS public.account_dynamic_recommendation_id_seq;

DROP TABLE IF EXISTS public.dynamic_recommendation_translation;

DROP SEQUENCE IF EXISTS public.dynamic_recommendation_translation_id_seq;

DROP TABLE IF EXISTS public.dynamic_recommendation;

-- static recommendation
DROP TABLE IF EXISTS public.static_recommendation;

DROP SEQUENCE IF EXISTS public.static_recommendation_id_seq;

DROP TABLE IF EXISTS public.static_recommendation_category;

-- favourite
DROP TABLE IF EXISTS public.favourite_account;

DROP TABLE IF EXISTS public.favourite_group;

DROP TABLE IF EXISTS public.favourite;

DROP SEQUENCE IF EXISTS public.favourite_id_seq;

-- cluster
DROP TABLE IF EXISTS public.group_cluster;

DROP TABLE IF EXISTS public."cluster";

DROP SEQUENCE IF EXISTS public.cluster_id_seq;

-- group
DROP TABLE IF EXISTS public.group_member;

DROP SEQUENCE IF EXISTS public.group_member_id_seq;

DROP TABLE IF EXISTS public.group_set;

DROP TABLE IF EXISTS public.group_community;

DROP TABLE IF EXISTS public."group";

DROP SEQUENCE IF EXISTS public.group_id_seq;

-- device
DROP TABLE IF EXISTS public.device_meter;

DROP TABLE IF EXISTS public.device_amphiro_config;

DROP TABLE IF EXISTS public.device_amphiro_config_default;

DROP SEQUENCE IF EXISTS public.device_amphiro_config_id_seq;

DROP TABLE IF EXISTS public.device_amphiro_permission;

DROP SEQUENCE IF EXISTS public.device_amphiro_permission_id_seq;

DROP TABLE IF EXISTS public.device_amphiro;

DROP TABLE IF EXISTS public.device_property;

DROP SEQUENCE IF EXISTS public.device_property_id_seq;

DROP TABLE IF EXISTS public.device;

DROP SEQUENCE IF EXISTS public.device_id_seq;

-- role
DROP TABLE IF EXISTS public.account_role;

DROP SEQUENCE IF EXISTS public.account_role_id_seq;

DROP TABLE IF EXISTS public.role;

DROP SEQUENCE IF EXISTS public.role_id_seq;

-- account white list
DROP TABLE IF EXISTS public.account_white_list;

DROP SEQUENCE IF EXISTS public.account_white_list_id_seq;

-- account
DROP TABLE IF EXISTS public.account_profile_history;

DROP SEQUENCE IF EXISTS public.account_profile_history_id_seq;

DROP TABLE IF EXISTS public.account_profile;

DROP TABLE IF EXISTS public.account;

DROP SEQUENCE IF EXISTS public.account_id_seq;

-- utility
DROP TABLE IF EXISTS public.utility;

DROP SEQUENCE IF EXISTS public.utility_id_seq;
