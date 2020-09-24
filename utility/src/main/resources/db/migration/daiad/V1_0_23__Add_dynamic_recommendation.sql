-- Add another kind of dynamic recommendation: #1000 - Insight A.1

INSERT INTO dynamic_recommendation ("id", "mode", "priority") VALUES(1000, 'BOTH', 5);

-- Must drop and recreate certain views that depend on dynamic_recommendation_translation columns

DROP VIEW recommendation_analytics;

-- Increase column size for "title"

ALTER TABLE dynamic_recommendation_translation 
    ALTER COLUMN "title" TYPE varchar(256);

-- Add translations for #1000 (Insight A.1)

INSERT INTO 
    dynamic_recommendation_translation(
        id,
        dynamic_recommendation_id, 
        locale, 
        title, 
        description
    ) VALUES (
        13,
        1000, 
        'en', 
        '{percentage}% {changetype} than your {weekday} average.\n{consumption} vs. the average {average_consumption}', 
        'This Monday you spent 20% more than you usually spend on Monday'
    );

INSERT INTO 
    dynamic_recommendation_translation(
        id,
        dynamic_recommendation_id, 
        locale, 
        title, 
        description
    ) VALUES (
        14,
        1000, 
        'es', 
        '{percentage}% {changetype} than your {weekday} average.\n{consumption} vs. the average {average_consumption}', 
        'This Monday you spent 20% more than you usually spend on Monday'
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

