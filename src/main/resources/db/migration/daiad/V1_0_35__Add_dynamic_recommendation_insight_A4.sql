-- Add new kinds of dynamic recommendation (Insight A.4)

insert into dynamic_recommendation ("id", "mode", "priority") values 
    (1010, 'BOTH', 5),
    (1011, 'BOTH', 5),
    (1012, 'BOTH', 5);
    
-- Add translations for Insight A.4

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    33,
    1010,
    'en',
    'Highest consumption during morning',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    34,
    1010,
    'es',
    'Highest consumption during morning',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);


insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    35,
    1011,
    'en',
    'Highest consumption during afternoon',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    36,
    1011,
    'es',
    'Highest consumption during afternoon',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);


insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    37,
    1012,
    'en',
    'Highest consumption during night',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);

insert into dynamic_recommendation_translation (id, dynamic_recommendation_id, locale, title, description) values (
    38,
    1012,
    'es',
    'Highest consumption during night',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);

