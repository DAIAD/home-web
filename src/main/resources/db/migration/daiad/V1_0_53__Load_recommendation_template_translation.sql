
TRUNCATE TABLE public.recommendation_template_translation;

--
-- Load public.recommendation_template_translation
-- 
 
INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LESS_SHOWER_TIME'),
        'en',
        'Spend 1 less minute in the shower and save <h1>{integer1}</h1> liters',
        'You are spending 50% more time in the shower than others. You could spend just 2 less minutes per shower and save up to <h1>{integer2}</h1> liters a year. Why not use our shower timer?'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LESS_SHOWER_TIME'),
        'es',
        'Reduce en un minut tu ducha y ahorra <h1>{integer1}</h1> litros',
        'Estás gastando un 50% más de agua en la ducha que otrοs. Reduciendo tu ducha en dos minutos puedes ahorrar <h1>{integer2}</h1> litros en un año. ¿Por qué no usar un contador en la ducha?'
    ); 


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LOWER_TEMPERATURE'),
        'en',
        'You could save <h1>{money1,number,currency}</h1> if you used a bit less hot water in the shower',
        'You are using a bit more hot water in the shower than others. If you reduced your shower temperature by 2 degrees you would save up to <h1>{money2,number,currency}</h1> in a year! Do you want us to remind you next time you take a shower?'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LOWER_TEMPERATURE'),
        'es',
        'Puedes ahorrar <h1>{money1,number,currency}</h1> si usas agua menos caliente en la ducha',
        'Estás utilizando agua más caliente que otros. ¡Reduciendo la temperatura de tu ducha en 2 grados ahorrarás <h1>{money2,number,currency}</h1> en un año! ¿Quiéres que te lo recordemos la próxima vez que te duches?'
    ); 


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LOWER_FLOW'),
        'en',
        'Reduce the water flow in the shower and save <h1>{integer1}</h1> liters',
        'You can slightly reduce the flow of water when you take a shower, and save up to <h1>{integer2}</h1> liters in a year!'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LOWER_FLOW'),
        'es',
        'Reduce el caudal de agua de tu ducha y ahorra <h1>{integer1}</h1> litros',
        '¡Puedes reducir un poco el caudal de tu ducha y ahorrar hasta <h1>{integer2}</h1> litros en un año!'
    ); 


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('CHANGE_SHOWERHEAD'),
        'en',
        'Change your showerhead and save <h1>{integer1}</h1> liters',
        'Why not change your shower-head with a more efficient one? It could save you up to <h1>{integer2}</h1> liters a year. The new shower head will provide you with an equally enjoyable shower. Do you want to find out more?'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('CHANGE_SHOWERHEAD'),
        'es',
        'Renueva el teléfono de la ducha y ahorra <h1>{integer1}</h1> litros',
        '¿Por qué no renovar el teléfono de la ducha con una más eficiente? Puedes ahorrar hasta <h1>{integer2}</h1> litros al año. Los nuevos modelos proporcionan una sensación de ducha similar pero con un menor consumo.'
    ); 


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('CHANGE_SHAMPOO'),
        'en',
        'Have you considered changing your shampoo?',
        'You are using <h1>{integer1}</h1>% more water than others. You may be using a shampoo or wash that foams excessively and is difficult to rinse. This does not mean it works better than others, but just that it leads to more water use. Perhaps try and use another product, easier to rinse, and more water friendly.'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('CHANGE_SHAMPOO'),
        'es',
        '¿Has considerado cambiar de champú?',
        'Estás consumiendo un <h1>{integer1}</h1>% más de agua que. Puede que estés utilizando demasiado champú o gel alargando así tu ducha. Puedes probar a usar otro que sea más sencillo de aclarar.'
    );

    
INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('REDUCE_FLOW_WHEN_NOT_NEEDED'),
        'en',
        'When showering, reduce the water flow when you do not need it',
        'Try turning the water off in the shower when you do not actually need it. You could save up to <h1>{integer1}</h1> liters per year this way! You might be surprised to find this quite comfortable, as the bathroom will already be quite warm. Try it once and see how easy it is!'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('REDUCE_FLOW_WHEN_NOT_NEEDED'),
        'es',
        'Cuando te duches, cierra el grifo si no necesitas agua',
        'Intenta cerrar el grifo de la ducha cuando no lo necesitas. Puedes ahorrar <h1>{integer1}</h1> litros cada año! Te sorprenderás al comprobar que aún apagando el grifo, el baño se mantiene caliente y ahorras agua a la vez.'
    ); 


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your {day,date,EEEE} average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your {day,date,EEEE} average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your {day,date,EEEE} average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your {day,date,EEEE} average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_DAILY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your daily average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_DAILY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your daily average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_DAILY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your daily average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_DAILY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your daily average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_MORNING_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in morning consumption',
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_MORNING_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in morning consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_MORNING_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in morning consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_MORNING_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in morning consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in afternoon consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in afternoon consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in afternoon consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in afternoon consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_NIGHT_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in night consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_NIGHT_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in night consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_NIGHT_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in night consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_NIGHT_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in night consumption',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING'),
        'en',
        'Highest consumption during morning',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING'),
        'es',
        'Highest consumption during morning',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON'),
        'en',
        'Highest consumption during afternoon',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON'),
        'es',
        'Highest consumption during afternoon',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT'),
        'en',
        'Highest consumption during night',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT'),
        'es',
        'Highest consumption during night',
        '<i>morning</i>: {morning_percentage}% - <i>afternoon</i>: {afternoon_percentage}% - <i>night</i>: {night_percentage}%'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_WEEKLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your weekly average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_WEEKLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your weekly average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_WEEKLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your weekly average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_WEEKLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your weekly average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_MONTHLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your monthly average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_MONTHLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your monthly average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_MONTHLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your monthly average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_MONTHLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your monthly average',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than previous week',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than previous week',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than previous week',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than previous week',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than previous month',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than previous month',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than previous month',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than previous month',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_DAYOFWEEK_CONSUMPTION_PEAK'),
        'en',
        '{day,date,EEEE} is your peak day',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_DAYOFWEEK_CONSUMPTION_PEAK'),
        'es',
        '{day,date,EEEE} is your peak day',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_DAYOFWEEK_CONSUMPTION_LOW'),
        'en',
        '{day,date,EEEE} is your low day',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_DAYOFWEEK_CONSUMPTION_LOW'),
        'es',
        '{day,date,EEEE} is your low day',
        '{consumption,number,.#}lt vs. the average {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_MORE_ON_WEEKEND'),
        'en',
        '{percent_change}% more water on weekends',
        '{weekend_consumption,number,.#}lt vs. {weekday_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_MORE_ON_WEEKEND'),
        'es',
        '{percent_change}% more water on weekends',
        '{weekend_consumption,number,.#}lt vs. {weekday_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_LESS_ON_WEEKEND'),
        'en',
        '{percent_change}% less water on weekends',
        '{weekend_consumption,number,.#}lt vs. {weekday_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_LESS_ON_WEEKEND'),
        'es',
        '{percent_change}% less water on weekends',
        '{weekend_consumption,number,.#}lt vs. {weekday_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_MONTHLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than same month last year',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_MONTHLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than same month last year',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_MONTHLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than same month last year',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_MONTHLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than same month last year',
        '{consumption,number,.#}lt vs. {previous_consumption,number,.#}lt'
    );

