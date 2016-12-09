-- Add new kinds of dynamic recommendation (Insight B.3)

insert into dynamic_recommendation ("id", "mode", "priority") values 
    (1108, 'BOTH', 5),
    (1109, 'BOTH', 5);
    
-- Add translations for Insight B.3
    
insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1108,
    'en',
    '{day_of_week} is your peak day',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1108,
    'es',
    '{day_of_week} is your peak day',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1109,
    'en',
    '{day_of_week} is your low day',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1109,
    'es',
    '{day_of_week} is your low day',
    '{consumption}lt vs. the average {average_consumption}lt'
);