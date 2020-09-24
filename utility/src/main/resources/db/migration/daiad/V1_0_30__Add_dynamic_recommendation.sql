-- Add new kinds of dynamic recommendation (Insight A.2)

INSERT INTO dynamic_recommendation ("id", "mode", "priority") VALUES(1002, 'BOTH', 5);
INSERT INTO dynamic_recommendation ("id", "mode", "priority") VALUES(1003, 'BOTH', 5);

-- Add translations for Insight A.2

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    17,
    1002,
    'en',
    '{percent_change}% more than your average',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    18,
    1002,
    'es',
    '{percent_change}% more than your average',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    19,
    1003,
    'en',
    '{percent_change}% less than your average',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    20,
    1003,
    'es',
    '{percent_change}% less than your average',
    '{consumption} vs. the average {average_consumption}'
);