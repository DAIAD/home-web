--
-- Definitions for dynamic recommendation types
--

delete from dynamic_recommendation;
delete from dynamic_recommendation_translation;

insert into dynamic_recommendation values (1, 'AMPHIRO',5);
insert into dynamic_recommendation values (2, 'AMPHIRO',5);
insert into dynamic_recommendation values (3, 'AMPHIRO',5);
insert into dynamic_recommendation values (4, 'AMPHIRO',5);
insert into dynamic_recommendation values (5, 'AMPHIRO',5);
insert into dynamic_recommendation values (6, 'AMPHIRO',5);

-- Insight A.1

insert into dynamic_recommendation values (1000, 'BOTH', 5); 
insert into dynamic_recommendation values (1001, 'BOTH', 5);

-- Insight A.2

insert into dynamic_recommendation values (1002, 'BOTH', 5);
insert into dynamic_recommendation values (1003, 'BOTH', 5);

-- Insight A.3

insert into dynamic_recommendation ("id", "mode", "priority") values 
    (1004, 'BOTH', 5),
    (1005, 'BOTH', 5),
    (1006, 'BOTH', 5),
    (1007, 'BOTH', 5),
    (1008, 'BOTH', 5),
    (1009, 'BOTH', 5);

-- Insight A.4

insert into dynamic_recommendation ("id", "mode", "priority") values 
    (1010, 'BOTH', 5),
    (1011, 'BOTH', 5),
    (1012, 'BOTH', 5);

-- Insight B.1

insert into dynamic_recommendation ("id", "mode", "priority") values 
    (1100, 'BOTH', 5),
    (1101, 'BOTH', 5),
    (1102, 'BOTH', 5),
    (1103, 'BOTH', 5);

-- Insight B.2

insert into dynamic_recommendation ("id", "mode", "priority") values 
    (1104, 'BOTH', 5),
    (1105, 'BOTH', 5),
    (1106, 'BOTH', 5),
    (1107, 'BOTH', 5);

-- Insight B.3

insert into dynamic_recommendation ("id", "mode", "priority") values 
    (1108, 'BOTH', 5),
    (1109, 'BOTH', 5);

--
-- Template messages for dynamic recommendations
--

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1,
    'en',
    'Spend 1 less minute in the shower and save <h1>{integer1}</h1> liters',
    'You are spending 50% more time in the shower than others. You could spend just 2 less minutes per shower and save up to <h1>{integer2}</h1> liters a year. Why not use our shower timer?'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    2,
    'en',
    'You could save <h1>{currency1}</h1> if you used a bit less hot water in the shower',
    'You are using a bit more hot water in the shower than others. If you reduced your shower temperature by 2 degrees you would save up to <h1>{currency2}</h1> in a year! Do you want us to remind you next time you take a shower?'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    3,
    'en',
    'Reduce the water flow in the shower and save <h1>{integer1}</h1> liters',
    'You can slightly reduce the flow of water when you take a shower, and save up to <h1>{integer2}</h1> liters in a year!'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    4,
    'en',
    'Change your showerhead and save <h1>{integer1}</h1> liters',
    'Why not change your shower-head with a more efficient one? It could save you up to <h1>{integer2}</h1> liters a year. The new shower head will provide you with an equally enjoyable shower. Do you want to find out more?'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    5,
    'en',
    'Have you considered changing your shampoo?',
    'You are using <h1>{integer1}</h1>% more water than others. You may be using a shampoo or wash that foams excessively and is difficult to rinse. This does not mean it works better than others, but just that it leads to more water use. Perhaps try and use another product, easier to rinse, and more water friendly.'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    6,
    'en',
    'When showering, reduce the water flow when you do not need it',
    'Try turning the water off in the shower when you do not actually need it. You could save up to <h1>{integer1}</h1> liters per year this way! You might be surprised to find this quite comfortable, as the bathroom will already be quite warm. Try it once and see how easy it is!'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1,
    'es',
    'Reduce en un minut tu ducha y ahorra <h1>{integer1}</h1> litros',
    'Estás gastando un 50% más de agua en la ducha que otrοs. Reduciendo tu ducha en dos minutos puedes ahorrar <h1>{integer2}</h1> litros en un año. ¿Por qué no usar un contador en la ducha?'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    2,
    'es',
    'Puedes ahorrar <h1>{currency1}</h1> si usas agua menos caliente en la ducha.',
    'Estás utilizando agua más caliente que otros. ¡Reduciendo la temperatura de tu ducha en 2 grados ahorrarás <h1>{currency2}</h1> en un año! ¿Quiéres que te lo recordemos la próxima vez que te duches?'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    3,
    'es',
    'Reduce el caudal de agua de tu ducha y ahorra <h1>{integer1}</h1> litros',
    '¡Puedes reducir un poco el caudal de tu ducha y ahorrar hasta <h1>{integer2}</h1> litros en un año!'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    4,
    'es',
    'Renueva el teléfono de la ducha y ahorra <h1>{integer1}</h1> litros',
    '¿Por qué no renovar el teléfono de la ducha con una más eficiente? Puedes ahorrar hasta <h1>{integer2}</h1> litros al año. Los nuevos modelos proporcionan una sensación de ducha similar pero con un menor consumo.'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    5,
    'es',
    '¿Has considerado cambiar de champú?',
    'Estás consumiendo un <h1>{integer1}</h1>% más de agua que. Puede que estés utilizando demasiado champú o gel alargando así tu ducha. Puedes probar a usar otro que sea más sencillo de aclarar.'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    6,
    'es',
    'Cuando te duches, cierra el grifo si no necesitas agua.',
    'Intenta cerrar el grifo de la ducha cuando no lo necesitas. Puedes ahorrar <h1>{integer1}</h1> litros cada año! Te sorprenderás al comprobar que aún apagando el grifo, el baño se mantiene caliente y ahorras agua a la vez.'
);

-- Insight A.1

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1000,
    'en',
    '{percent_change}% more than your {day_of_week} average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1000,
    'es',
    '{percent_change}% more than your {day_of_week} average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1001,
    'en',
    '{percent_change}% less than your {day_of_week} average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1001,
    'es',
    '{percent_change}% less than your {day_of_week} average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

-- Insight A.2

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1002,
    'en',
    '{percent_change}% more than your average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1002,
    'es',
    '{percent_change}% more than your average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1003,
    'en',
    '{percent_change}% less than your average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1003,
    'es',
    '{percent_change}% less than your average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

-- Insight A.3

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1004,
    'en',
    '{percent_change}% increase in morning consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1004,
    'es',
    '{percent_change}% increase in morning consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1005,
    'en',
    '{percent_change}% decrease in morning consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1005,
    'es',
    '{percent_change}% decrease in morning consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1006,
    'en',
    '{percent_change}% increase in afternoon consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1006,
    'es',
    '{percent_change}% increase in afternoon consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1007,
    'en',
    '{percent_change}% decrease in afternoon consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1007,
    'es',
    '{percent_change}% decrease in afternoon consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1008,
    'en',
    '{percent_change}% increase in night consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1008,
    'es',
    '{percent_change}% increase in night consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1009,
    'en',
    '{percent_change}% decrease in night consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1009,
    'es',
    '{percent_change}% decrease in night consumption',
    '{consumption}lt vs. the average {average_consumption}lt'
);

-- Insight A.4 

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1010,
    'en',
    'Highest consumption during morning',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1010,
    'es',
    'Highest consumption during morning',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);


insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1011,
    'en',
    'Highest consumption during afternoon',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1011,
    'es',
    'Highest consumption during afternoon',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);


insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1012,
    'en',
    'Highest consumption during night',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1012,
    'es',
    'Highest consumption during night',
    '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
);

-- Insight B.1

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1100,
    'en',
    '{percent_change}% more than your weekly average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1100,
    'es',
    '{percent_change}% more than your weekly average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1101,
    'en',
    '{percent_change}% less than your weekly average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1101,
    'es',
    '{percent_change}% less than your weekly average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1102,
    'en',
    '{percent_change}% more than your monthly average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1102,
    'es',
    '{percent_change}% more than your monthly average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1103,
    'en',
    '{percent_change}% less than your monthly average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1103,
    'es',
    '{percent_change}% less than your monthly average',
    '{consumption}lt vs. the average {average_consumption}lt'
);

-- Insight B.2

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1104,
    'en',
    '{percent_change}% more than previous week',
    '{consumption}lt vs. {previous_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1104,
    'es',
    '{percent_change}% more than previous week',
    '{consumption}lt vs. {previous_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1105,
    'en',
    '{percent_change}% less than previous week',
    '{consumption}lt vs. {previous_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1105,
    'es',
    '{percent_change}% less than previous week',
    '{consumption}lt vs. {previous_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1106,
    'en',
    '{percent_change}% more than previous month',
    '{consumption}lt vs. {previous_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1106,
    'es',
    '{percent_change}% more than previous month',
    '{consumption}lt vs. {previous_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1107,
    'en',
    '{percent_change}% less than previous month',
    '{consumption}lt vs. {previous_consumption}lt'
);

insert into dynamic_recommendation_translation (dynamic_recommendation_id, locale, title, description) values (
    1107,
    'es',
    '{percent_change}% less than previous month',
    '{consumption}lt vs. {previous_consumption}lt'
);

-- Insight B.3

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
