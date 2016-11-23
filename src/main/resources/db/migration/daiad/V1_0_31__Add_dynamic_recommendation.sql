-- Add new kinds of dynamic recommendation (Insight A.3)

insert into dynamic_recommendation ("id", "mode", "priority") values 
    (1004, 'BOTH', 5),
    (1005, 'BOTH', 5),
    (1006, 'BOTH', 5),
    (1007, 'BOTH', 5),
    (1008, 'BOTH', 5),
    (1009, 'BOTH', 5);

-- Add translations for Insight A.3
    
insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    21,
    1004,
    'en',
    '{percent_change}% increase in morning consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    22,
    1004,
    'es',
    '{percent_change}% increase in morning consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    23,
    1005,
    'en',
    '{percent_change}% decrease in morning consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    24,
    1005,
    'es',
    '{percent_change}% decrease in morning consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    25,
    1006,
    'en',
    '{percent_change}% increase in afternoon consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    26,
    1006,
    'es',
    '{percent_change}% increase in afternoon consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    27,
    1007,
    'en',
    '{percent_change}% decrease in afternoon consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    28,
    1007,
    'es',
    '{percent_change}% decrease in afternoon consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    29,
    1008,
    'en',
    '{percent_change}% increase in night consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    30,
    1008,
    'es',
    '{percent_change}% increase in night consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    31,
    1009,
    'en',
    '{percent_change}% decrease in night consumption',
    '{consumption} vs. the average {average_consumption}'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    32,
    1009,
    'es',
    '{percent_change}% decrease in night consumption',
    '{consumption} vs. the average {average_consumption}'
);
