--
-- Table public.recommendation_type
--

CREATE TABLE public.recommendation_type
(
    id integer NOT NULL,
    name character varying(128) NOT NULL,
    device character varying(128),
    priority integer,
    CONSTRAINT pk_recommendation_type PRIMARY KEY (id),
    CONSTRAINT uq_recommendation_type_name UNIQUE (name)
);

INSERT INTO public.recommendation_type (id, name, device, priority) VALUES
    (1, 'LESS_SHOWER_TIME', 'AMPHIRO', 5),
    (2, 'LOWER_TEMPERATURE', 'AMPHIRO', 5),
    (3, 'LOWER_FLOW', 'AMPHIRO', 5),
    (4, 'CHANGE_SHOWERHEAD', 'AMPHIRO', 5),
    (5, 'CHANGE_SHAMPOO', 'AMPHIRO', 5),
    (6, 'REDUCE_FLOW_WHEN_NOT_NEEDED', 'AMPHIRO', 5),
    (1001, 'INSIGHT_A1', null, 5),
    (1002, 'INSIGHT_A2', null, 5),
    (1003, 'INSIGHT_A3', null, 5),
    (1004, 'INSIGHT_A4', null, 5),
    (1005, 'INSIGHT_B1', null, 5),
    (1006, 'INSIGHT_B2', null, 5),
    (1007, 'INSIGHT_B3', null, 5),
    (1008, 'INSIGHT_B4', null, 5),
    (1009, 'INSIGHT_B5', null, 5);

--
-- Table public.recommendation_message
-- 

CREATE SEQUENCE public.recommendation_message_id_seq
    INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;

CREATE TABLE public.recommendation_message 
(
    id integer NOT NULL DEFAULT nextval('recommendation_message_id_seq'::regclass),
    recommendation_type integer NOT NULL,
    template_name character varying(128) NOT NULL,
    locale character(2) NOT NULL,
    title character varying(256),
    description character varying,
    image_link character varying(256),
    CONSTRAINT pk_recommendation_message PRIMARY KEY (id),
    CONSTRAINT fk_recommendation_message_type FOREIGN KEY (recommendation_type)
        REFERENCES public.recommendation_type (id) MATCH SIMPLE
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uq_recommendation_message_1 UNIQUE(template_name, locale)
);
 
INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'LESS_SHOWER_TIME'),
        'LESS_SHOWER_TIME',
        'en',
        'Spend 1 less minute in the shower and save <h1>{integer1}</h1> liters',
        'You are spending 50% more time in the shower than others. You could spend just 2 less minutes per shower and save up to <h1>{integer2}</h1> liters a year. Why not use our shower timer?'
    ); 

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'LESS_SHOWER_TIME'),
        'LESS_SHOWER_TIME',
        'es',
        'Reduce en un minut tu ducha y ahorra <h1>{integer1}</h1> litros',
        'Estás gastando un 50% más de agua en la ducha que otrοs. Reduciendo tu ducha en dos minutos puedes ahorrar <h1>{integer2}</h1> litros en un año. ¿Por qué no usar un contador en la ducha?'
    ); 


INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'LOWER_TEMPERATURE'),
        'LOWER_TEMPERATURE',
        'en',
        'You could save <h1>{currency1}</h1> if you used a bit less hot water in the shower',
        'You are using a bit more hot water in the shower than others. If you reduced your shower temperature by 2 degrees you would save up to <h1>{currency2}</h1> in a year! Do you want us to remind you next time you take a shower?'
    ); 

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'LOWER_TEMPERATURE'),
        'LOWER_TEMPERATURE',
        'es',
        'Puedes ahorrar <h1>{currency1}</h1> si usas agua menos caliente en la ducha',
        'Estás utilizando agua más caliente que otros. ¡Reduciendo la temperatura de tu ducha en 2 grados ahorrarás <h1>{currency2}</h1> en un año! ¿Quiéres que te lo recordemos la próxima vez que te duches?'
    ); 


INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'LOWER_FLOW'),
        'LOWER_FLOW',
        'en',
        'Reduce the water flow in the shower and save <h1>{integer1}</h1> liters',
        'You can slightly reduce the flow of water when you take a shower, and save up to <h1>{integer2}</h1> liters in a year!'
    ); 

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'LOWER_FLOW'),
        'LOWER_FLOW',
        'es',
        'Reduce el caudal de agua de tu ducha y ahorra <h1>{integer1}</h1> litros',
        '¡Puedes reducir un poco el caudal de tu ducha y ahorrar hasta <h1>{integer2}</h1> litros en un año!'
    ); 


INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'CHANGE_SHOWERHEAD'),
        'CHANGE_SHOWERHEAD',
        'en',
        'Change your showerhead and save <h1>{integer1}</h1> liters',
        'Why not change your shower-head with a more efficient one? It could save you up to <h1>{integer2}</h1> liters a year. The new shower head will provide you with an equally enjoyable shower. Do you want to find out more?'
    ); 

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'CHANGE_SHOWERHEAD'),
        'CHANGE_SHOWERHEAD',
        'es',
        'Renueva el teléfono de la ducha y ahorra <h1>{integer1}</h1> litros',
        '¿Por qué no renovar el teléfono de la ducha con una más eficiente? Puedes ahorrar hasta <h1>{integer2}</h1> litros al año. Los nuevos modelos proporcionan una sensación de ducha similar pero con un menor consumo.'
    ); 


INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'CHANGE_SHAMPOO'),
        'CHANGE_SHAMPOO',
        'en',
        'Have you considered changing your shampoo?',
        'You are using <h1>{integer1}</h1>% more water than others. You may be using a shampoo or wash that foams excessively and is difficult to rinse. This does not mean it works better than others, but just that it leads to more water use. Perhaps try and use another product, easier to rinse, and more water friendly.'
    ); 

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'CHANGE_SHAMPOO'),
        'CHANGE_SHAMPOO',
        'es',
        '¿Has considerado cambiar de champú?',
        'Estás consumiendo un <h1>{integer1}</h1>% más de agua que. Puede que estés utilizando demasiado champú o gel alargando así tu ducha. Puedes probar a usar otro que sea más sencillo de aclarar.'
    );

    
INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'REDUCE_FLOW_WHEN_NOT_NEEDED'),
        'REDUCE_FLOW_WHEN_NOT_NEEDED',
        'en',
        'When showering, reduce the water flow when you do not need it',
        'Try turning the water off in the shower when you do not actually need it. You could save up to <h1>{integer1}</h1> liters per year this way! You might be surprised to find this quite comfortable, as the bathroom will already be quite warm. Try it once and see how easy it is!'
    ); 

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'REDUCE_FLOW_WHEN_NOT_NEEDED'),
        'REDUCE_FLOW_WHEN_NOT_NEEDED',
        'es',
        'Cuando te duches, cierra el grifo si no necesitas agua',
        'Intenta cerrar el grifo de la ducha cuando no lo necesitas. Puedes ahorrar <h1>{integer1}</h1> litros cada año! Te sorprenderás al comprobar que aún apagando el grifo, el baño se mantiene caliente y ahorras agua a la vez.'
    ); 


INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A1'),
        'INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR',
        'en',
        '{percent_change}% more than your {day_of_week} average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A1'),
        'INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR',
        'es',
        '{percent_change}% more than your {day_of_week} average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A1'),
        'INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR',
        'en',
        '{percent_change}% less than your {day_of_week} average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A1'),
        'INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR',
        'es',
        '{percent_change}% less than your {day_of_week} average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );


INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A2'),
        'INSIGHT_A2_DAILY_CONSUMPTION_INCR',
        'en',
        '{percent_change}% more than your average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A2'),
        'INSIGHT_A2_DAILY_CONSUMPTION_INCR',
        'es',
        '{percent_change}% more than your average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A2'),
        'INSIGHT_A2_DAILY_CONSUMPTION_DECR',
        'en',
        '{percent_change}% less than your average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A2'),
        'INSIGHT_A2_DAILY_CONSUMPTION_DECR',
        'es',
        '{percent_change}% less than your average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );


INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_MORNING_CONSUMPTION_INCR',
        'en',
        '{percent_change}% increase in morning consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_MORNING_CONSUMPTION_INCR',
        'es',
        '{percent_change}% increase in morning consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_MORNING_CONSUMPTION_DECR',
        'en',
        '{percent_change}% decrease in morning consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_MORNING_CONSUMPTION_DECR',
        'es',
        '{percent_change}% decrease in morning consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR',
        'en',
        '{percent_change}% increase in afternoon consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR',
        'es',
        '{percent_change}% increase in afternoon consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR',
        'en',
        '{percent_change}% decrease in afternoon consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR',
        'es',
        '{percent_change}% decrease in afternoon consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_NIGHT_CONSUMPTION_INCR',
        'en',
        '{percent_change}% increase in night consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_NIGHT_CONSUMPTION_INCR',
        'es',
        '{percent_change}% increase in night consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_NIGHT_CONSUMPTION_DECR',
        'en',
        '{percent_change}% decrease in night consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A3'),
        'INSIGHT_A3_NIGHT_CONSUMPTION_DECR',
        'es',
        '{percent_change}% decrease in night consumption',
        '{consumption}lt vs. the average {average_consumption}lt'
    );


INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A4'),
        'INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING',
        'en',
        'Highest consumption during morning',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A4'),
        'INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING',
        'es',
        'Highest consumption during morning',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A4'),
        'INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON',
        'en',
        'Highest consumption during afternoon',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A4'),
        'INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON',
        'es',
        'Highest consumption during afternoon',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A4'),
        'INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT',
        'en',
        'Highest consumption during night',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_A4'),
        'INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT',
        'es',
        'Highest consumption during night',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B1'),
        'INSIGHT_B1_WEEKLY_CONSUMPTION_INCR',
        'en',
        '{percent_change}% more than your weekly average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B1'),
        'INSIGHT_B1_WEEKLY_CONSUMPTION_INCR',
        'es',
        '{percent_change}% more than your weekly average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B1'),
        'INSIGHT_B1_WEEKLY_CONSUMPTION_DECR',
        'en',
        '{percent_change}% less than your weekly average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B1'),
        'INSIGHT_B1_WEEKLY_CONSUMPTION_DECR',
        'es',
        '{percent_change}% less than your weekly average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B1'),
        'INSIGHT_B1_MONTHLY_CONSUMPTION_INCR',
        'en',
        '{percent_change}% more than your monthly average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B1'),
        'INSIGHT_B1_MONTHLY_CONSUMPTION_INCR',
        'es',
        '{percent_change}% more than your monthly average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B1'),
        'INSIGHT_B1_MONTHLY_CONSUMPTION_DECR',
        'en',
        '{percent_change}% less than your monthly average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B1'),
        'INSIGHT_B1_MONTHLY_CONSUMPTION_DECR',
        'es',
        '{percent_change}% less than your monthly average',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B2'),
        'INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR',
        'en',
        '{percent_change}% more than previous week',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B2'),
        'INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR',
        'es',
        '{percent_change}% more than previous week',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B2'),
        'INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR',
        'en',
        '{percent_change}% less than previous week',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B2'),
        'INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR',
        'es',
        '{percent_change}% less than previous week',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B2'),
        'INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR',
        'en',
        '{percent_change}% more than previous month',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B2'),
        'INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR',
        'es',
        '{percent_change}% more than previous month',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B2'),
        'INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR',
        'en',
        '{percent_change}% less than previous month',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B2'),
        'INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR',
        'es',
        '{percent_change}% less than previous month',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B3'),
        'INSIGHT_B3_DAYOFWEEK_CONSUMPTION_PEAK',
        'en',
        '{day_of_week} is your peak day',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B3'),
        'INSIGHT_B3_DAYOFWEEK_CONSUMPTION_PEAK',
        'es',
        '{day_of_week} is your peak day',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B3'),
        'INSIGHT_B3_DAYOFWEEK_CONSUMPTION_LOW',
        'en',
        '{day_of_week} is your low day',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B3'),
        'INSIGHT_B3_DAYOFWEEK_CONSUMPTION_LOW',
        'es',
        '{day_of_week} is your low day',
        '{consumption}lt vs. the average {average_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B4'),
        'INSIGHT_B4_MORE_ON_WEEKEND',
        'en',
        '{percent_diff}% more water on weekends',
        '{weekend_consumption}lt vs. {weekday_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B4'),
        'INSIGHT_B4_MORE_ON_WEEKEND',
        'es',
        '{percent_diff}% more water on weekends',
        '{weekend_consumption}lt vs. {weekday_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B4'),
        'INSIGHT_B4_LESS_ON_WEEKEND',
        'en',
        '{percent_diff}% less water on weekends',
        '{weekend_consumption}lt vs. {weekday_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B4'),
        'INSIGHT_B4_LESS_ON_WEEKEND',
        'es',
        '{percent_diff}% less water on weekends',
        '{weekend_consumption}lt vs. {weekday_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B5'),
        'INSIGHT_B5_MONTHLY_CONSUMPTION_INCR',
        'en',
        '{percent_change}% more than same month last year',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B5'),
        'INSIGHT_B5_MONTHLY_CONSUMPTION_INCR',
        'es',
        '{percent_change}% more than same month last year',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B5'),
        'INSIGHT_B5_MONTHLY_CONSUMPTION_DECR',
        'en',
        '{percent_change}% less than same month last year',
        '{consumption}lt vs. {previous_consumption}lt'
    );

INSERT INTO public.recommendation_message 
    (recommendation_type, template_name, locale, title, description)
    VALUES
    (
        (SELECT id FROM recommendation_type WHERE name = 'INSIGHT_B5'),
        'INSIGHT_B5_MONTHLY_CONSUMPTION_DECR',
        'es',
        '{percent_change}% less than same month last year',
        '{consumption}lt vs. {previous_consumption}lt'
    );

--
-- Table account_recommendation
--

CREATE SEQUENCE public.account_recommendation_id_seq
    INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;

CREATE TABLE public.account_recommendation
(
    id integer NOT NULL DEFAULT nextval('account_recommendation_id_seq'::regclass),
    account_id integer,
    template_name integer NOT NULL,
    created_on timestamp without time zone,
    acknowledged_on timestamp without time zone,
    receive_acknowledged_on timestamp without time zone,
    CONSTRAINT pk_account_recommendation PRIMARY KEY (id),
    CONSTRAINT fk_account_recommendation_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

--
-- Table account_recommendation_parameter
--


CREATE SEQUENCE public.account_recommendation_parameter_id_seq
    INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;

CREATE TABLE public.account_recommendation_parameter
(
    id integer NOT NULL DEFAULT nextval('account_recommendation_parameter_id_seq'::regclass),
    account_recommendation_id integer,
    key character varying(64),
    value character varying,
    CONSTRAINT pk_account_recommendation_parameter PRIMARY KEY (id),
    CONSTRAINT fk_account_recommendation_parameter FOREIGN KEY (account_recommendation_id)
        REFERENCES public.account_recommendation (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);


