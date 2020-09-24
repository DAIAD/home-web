
-- Add new kinds of dynamic recommendation (Insight A.1)

DELETE FROM dynamic_recommendation WHERE id = 1000 OR id = 1001;

INSERT INTO dynamic_recommendation ("id", "mode", "priority") VALUES(1000, 'BOTH', 5);
INSERT INTO dynamic_recommendation ("id", "mode", "priority") VALUES(1001, 'BOTH', 5);

-- Must drop and recreate certain views that depend on dynamic_recommendation_translation columns

DROP VIEW recommendation_analytics;

-- Add translations for Insight A.1

DELETE FROM dynamic_recommendation_translation WHERE 
   dynamic_recommendation_id = 1000 OR dynamic_recommendation_id = 1001;

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    13,
    1000,
    'en',
    '{percent_change}% more than your {day_of_week} average',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    14,
    1000,
    'es',
    '{percent_change}% more than your {day_of_week} average',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    15,
    1001,
    'en',
    '{percent_change}% less than your {day_of_week} average',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    16,
    1001,
    'es',
    '{percent_change}% less than your {day_of_week} average',
    '{consumption} vs. the average {average_consumption}'
);


-- Recreate views

CREATE OR REPLACE VIEW recommendation_analytics AS 
 SELECT r.id,
    rt.title,
    rt.description,
    rt.locale,
    count(1) AS total
   FROM dynamic_recommendation r
     LEFT JOIN dynamic_recommendation_translation rt ON r.id = rt.dynamic_recommendation_id
     LEFT JOIN account_dynamic_recommendation ar ON ar.dynamic_recommendation_id = r.id
     LEFT JOIN account ac ON ar.account_id = ac.id
  GROUP BY r.id, rt.title, rt.description, rt.locale;

