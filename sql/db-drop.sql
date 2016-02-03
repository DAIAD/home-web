-- alert
DROP TABLE public.account_alert_property;

DROP SEQUENCE public.account_alert_property_id_seq;

DROP TABLE public.account_alert;

DROP SEQUENCE public.account_alert_id_seq;

DROP TABLE public.alert_translation;

DROP SEQUENCE public.alert_translation_id_seq;

DROP TABLE public.alert;

-- announcement
DROP TABLE public.account_announcement;

DROP SEQUENCE public.account_announcement_id_seq;

DROP TABLE public.announcement_translation;

DROP SEQUENCE public.announcement_translation_id_seq;

DROP TABLE public.announcement_channel;

DROP TABLE public.announcement;

-- channel
DROP TABLE public.channel;

-- dynamic recommendation
DROP TABLE public.account_dynamic_recommendation_property;

DROP SEQUENCE public.account_dynamic_recommendation_property_id_seq;

DROP TABLE public.account_dynamic_recommendation;

DROP SEQUENCE public.account_dynamic_recommendation_id_seq;

DROP TABLE public.dynamic_recommendation_translation;

DROP SEQUENCE public.dynamic_recommendation_translation_id_seq;

DROP TABLE public.dynamic_recommendation;

-- static recommendation
DROP TABLE public.static_recommendation;

DROP SEQUENCE public.static_recommendation_id_seq;

DROP TABLE public.static_recommendation_category;

-- group
DROP TABLE public.group_member;

DROP SEQUENCE public.group_member_id_seq;

DROP TABLE public."group";

DROP SEQUENCE public.group_id_seq;

-- community
DROP TABLE public.community_member;

DROP SEQUENCE public.community_member_id_seq;

DROP TABLE public.community;

DROP SEQUENCE public.community_id_seq;

-- device
DROP TABLE public.device_meter;

DROP TABLE public.device_amphiro_config;

DROP SEQUENCE public.device_amphiro_config_id_seq;

DROP TABLE public.device_amphiro;

DROP TABLE public.device_property;

DROP SEQUENCE public.device_property_id_seq;

DROP TABLE public.device;

DROP SEQUENCE public.device_id_seq;

-- role
DROP TABLE public.account_role;

DROP SEQUENCE public.account_role_id_seq;

DROP TABLE public.role;

DROP SEQUENCE public.role_id_seq;

-- account white list
DROP TABLE public.account_white_list;

DROP SEQUENCE public.account_white_list_id_seq;

-- account
DROP TABLE public.account;

DROP SEQUENCE public.account_id_seq;

-- utility
DROP TABLE public.utility;

DROP SEQUENCE public.utility_id_seq;
