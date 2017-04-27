
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
        'Spend 1 less minute in the shower and save <h1>{annual_savings_1}</h1> liters',
        'You are spending 50% more time in the shower than others. You could spend just 2 less minutes per shower and save up to <h1>{annual_savings_2}</h1> liters a year. Why not use our shower timer?'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LESS_SHOWER_TIME'),
        'es',
        'Si reduces tu ducha en 1 minuto ahorrarás <h1>{annual_savings_1}</h1> litros al año',
        'Estás utilizando 50% más tiempo en ducharte que el resto. Reduciendo 2 minutos tu ducha ahorrarás <h1>{annual_savings_2}</h1> litros al año. ¿Por qué no utilizas el cronómetro de agua?'
    ); 


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LOWER_TEMPERATURE'),
        'en',
        'You could save <h1>{annual_savings_2,number,currency}</h1> if you used a bit less hot water in the shower',
        'You are using a bit more hot water in the shower than others. If you reduced your shower temperature by 2 degrees you would save up to <h1>{annual_savings_2,number,currency}</h1> in a year! Do you want us to remind you next time you take a shower?'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LOWER_TEMPERATURE'),
        'es',
        'Puedes ahorrar <h1>{annual_savings_2,number,currency}</h1> si no pones el agua tan caliente mientras te duchas',
        'La temperatura del agua de tu ducha es mayor que el promedio de usuarios. Reduciendo la temperatura del agua en 2 grados podrías llegar a ahorrar <h1>{annual_savings_2,number,currency}</h1> en un año. ¿Quieres que te lo recordemos en tu próxima ducha?'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LOWER_FLOW'),
        'en',
        'Reduce the water flow in the shower and save <h1>{annual_savings}</h1> liters',
        'You can slightly reduce the flow of water when you take a shower, and save up to <h1>{annual_savings}</h1> liters in a year!'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('LOWER_FLOW'),
        'es',
        'Abriendo menos el grifo de la ducha ahorrarás <h1>{annual_savings}</h1> litros al año',
        'Puedes reducir un poco el caudal de agua cuando te duchas, ahorrando así hasta <h1>{annual_savings}</h1> litros al año' 
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('CHANGE_SHOWERHEAD'),
        'en',
        'Change your showerhead and save <h1>{annual_savings}</h1> liters',
        'Why not change your shower-head with a more efficient one? It could save you up to <h1>{annual_savings}</h1> liters a year. The new shower head will provide you with an equally enjoyable shower. Do you want to find out more?'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('CHANGE_SHOWERHEAD'),
        'es',
        'Renueva el cabezal de tu ducha y ahorra <h1>{annual_savings}</h1> litros',
        '¿Por qué no renuevas el cabezal de tu ducha por un modelo más eficiente? Podrías ahorrar hasta <h1>{annual_savings}</h1> litros al año. Aunque consumas menos agua, será igual de placentera, ¿quieres saber más?' 
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('CHANGE_SHAMPOO'),
        'en',
        'Are you using the right amount of shampoo?',
        'You are using <h1>{percent_above_consumption,number,integer}</h1>% more water than others. You may be using more shampoo or wash that you really need. If you use so much shampoo you will need more water to clear it. Do you want to find out more?'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('CHANGE_SHAMPOO'),
        'es',
        '¿Estás utilizando la cantidad correcta de champú?',
        'Estás utilizando <h1>{percent_above_consumption,number,integer}</h1>% más de agua que el resto. Podrías estar utilizando demasiado Champú o gel. Si utilizas una cantidad excesiva necesitarás más agua para aclararlo. ¿Quieres saber más?'
    );


INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('REDUCE_FLOW_WHEN_NOT_NEEDED'),
        'en',
        'When showering, reduce the water flow when you do not need it',
        'Try turning the water off in the shower when you do not actually need it. You could save up to <h1>{annual_savings}</h1> liters per year this way! You might be surprised to find this quite comfortable, as the bathroom will already be quite warm. Try it once and see how easy it is!'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('REDUCE_FLOW_WHEN_NOT_NEEDED'),
        'es',
        'Cuando te duches, cierra el grifo si no necesitas agua',
        'Intenta cerrar el grifo de la ducha cuando no lo necesitas. Puedes ahorrar <h1>{annual_savings}</h1> litros cada año! Te sorprenderás al comprobar que aún apagando el grifo, el baño se mantiene caliente y ahorras agua a la vez.'
    ); 

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_METER_DAYOFWEEK_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your {day,date,EEEE} household average',
        'This {day,date,EEEE} you spent {consumption,number,.#}lt while your {day,date,EEEE} average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_METER_DAYOFWEEK_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your {day,date,EEEE} household average',
        'This {day,date,EEEE} you spent {consumption,number,.#}lt while your {day,date,EEEE} average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_METER_DAYOFWEEK_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your {day,date,EEEE} household average',
        'This {day,date,EEEE} you spent {consumption,number,.#}lt while your {day,date,EEEE} average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_METER_DAYOFWEEK_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your {day,date,EEEE} household average',
        'This {day,date,EEEE} you spent {consumption,number,.#}lt while your {day,date,EEEE} average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_SHOWER_DAYOFWEEK_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your {day,date,EEEE} shower average',
        'This {day,date,EEEE} you spent {consumption,number,.#}lt while your {day,date,EEEE} average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_SHOWER_DAYOFWEEK_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your {day,date,EEEE} shower average',
        'This {day,date,EEEE} you spent {consumption,number,.#}lt while your {day,date,EEEE} average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_SHOWER_DAYOFWEEK_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your {day,date,EEEE} shower average',
        'This {day,date,EEEE} you spent {consumption,number,.#}lt while your {day,date,EEEE} average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A1_SHOWER_DAYOFWEEK_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your {day,date,EEEE} shower average',
        'This {day,date,EEEE} you spent {consumption,number,.#}lt while your {day,date,EEEE} average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_METER_DAILY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your daily household average',
        'Yesterday you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average daily consumption of {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_METER_DAILY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your daily household average',
        'Yesterday you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average daily consumption of {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_METER_DAILY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your daily household average',
        'Yesterday you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average daily consumption of {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_METER_DAILY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your daily household average',
        'Yesterday you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average daily consumption of {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_SHOWER_DAILY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your daily shower average',
        'Yesterday you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average daily consumption of {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_SHOWER_DAILY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your daily shower average',
        'Yesterday you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average daily consumption of {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_SHOWER_DAILY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your daily shower average',
        'Yesterday you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average daily consumption of {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A2_SHOWER_DAILY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your daily shower average',
        'Yesterday you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average daily consumption of {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_MORNING_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in morning household consumption',
        'Yesterday morning you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_MORNING_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in morning household consumption',
        'Yesterday morning you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_MORNING_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in morning household consumption',
        'Yesterday morning you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_MORNING_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in morning household consumption',
        'Yesterday morning you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_AFTERNOON_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in afternoon household consumption',
        'Yesterday afternoon you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_AFTERNOON_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in afternoon household consumption',
        'Yesterday afternoon you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_AFTERNOON_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in afternoon household consumption',
        'Yesterday afternoon you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_AFTERNOON_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in afternoon household consumption',
        'Yesterday afternoon you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_NIGHT_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in night household consumption',
        'Yesterday night you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_NIGHT_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in night household consumption',
        'Yesterday night you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_NIGHT_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in night household consumption',
        'Yesterday night you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_METER_NIGHT_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in night household consumption',
        'Yesterday night you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_MORNING_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in morning shower consumption',
        'Yesterday morning you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_MORNING_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in morning shower consumption',
        'Yesterday morning you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_MORNING_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in morning shower consumption',
        'Yesterday morning you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_MORNING_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in morning shower consumption',
        'Yesterday morning you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_AFTERNOON_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in afternoon shower consumption',
        'Yesterday afternoon you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_AFTERNOON_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in afternoon shower consumption',
        'Yesterday afternoon you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_AFTERNOON_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in afternoon shower consumption',
        'Yesterday afternoon you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_AFTERNOON_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in afternoon shower consumption',
        'Yesterday afternoon you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_NIGHT_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% increase in night shower consumption',
        'Yesterday night you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_NIGHT_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% increase in night shower consumption',
        'Yesterday night you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_NIGHT_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% decrease in night shower consumption',
        'Yesterday night you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A3_SHOWER_NIGHT_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% decrease in night shower consumption',
        'Yesterday night you spent {consumption,number,.#}lt while your morning average is {average_consumption,number,.#}lt'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_MORNING'),
        'en',
        'Highest household consumption during morning',
        'On average, you consume most water during the morning. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_MORNING'),
        'es',
        'Highest household consumption during morning',
        'On average, you consume most water during the morning. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_AFTERNOON'),
        'en',
        'Highest household consumption during afternoon',
        'On average, you consume most water during the afternoon. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_AFTERNOON'),
        'es',
        'Highest household consumption during afternoon',
        'On average, you consume most water during the afternoon. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_NIGHT'),
        'en',
        'Highest household consumption during night',
        'On average, you consume most water during the night. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_NIGHT'),
        'es',
        'Highest household consumption during night',
        'On average, you consume most water during the night. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_MORNING'),
        'en',
        'Highest shower consumption during morning',
        'On average, you consume most water during the morning. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_MORNING'),
        'es',
        'Highest shower consumption during morning',
        'On average, you consume most water during the morning. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_AFTERNOON'),
        'en',
        'Highest shower consumption during afternoon',
        'On average, you consume most water during the afternoon. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_AFTERNOON'),
        'es',
        'Highest shower consumption during afternoon',
        'On average, you consume most water during the afternoon. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_NIGHT'),
        'en',
        'Highest shower consumption during night',
        'On average, you consume most water during the night. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_NIGHT'),
        'es',
        'Highest shower consumption during night',
        'On average, you consume most water during the night. Specifically, you consume {morning_percentage}% on morning, {afternoon_percentage}% on afternoon and {night_percentage}% on night.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_METER_WEEKLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your weekly household average',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average weekly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_METER_WEEKLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your weekly household average',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average weekly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_METER_WEEKLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your weekly household average',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average weekly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_METER_WEEKLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your weekly household average',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average weekly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_METER_MONTHLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your monthly household average',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average monthly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_METER_MONTHLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your monthly household average',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average monthly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_METER_MONTHLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your monthly household average',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average monthly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_METER_MONTHLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your monthly household average',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average monthly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_SHOWER_WEEKLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your weekly shower average',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average weekly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_SHOWER_WEEKLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your weekly shower average',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average weekly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_SHOWER_WEEKLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your weekly shower average',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average weekly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_SHOWER_WEEKLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your weekly shower average',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average weekly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_SHOWER_MONTHLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than your monthly shower average',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average monthly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_SHOWER_MONTHLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than your monthly shower average',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your average monthly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_SHOWER_MONTHLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than your monthly shower average',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average monthly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B1_SHOWER_MONTHLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than your monthly shower average',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your average monthly consumption of {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_METER_WEEKLY_PREV_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than previous week',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your household consumption of {previous_consumption,number,.#}lt the week before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_METER_WEEKLY_PREV_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than previous week',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your household consumption of {previous_consumption,number,.#}lt the week before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_METER_WEEKLY_PREV_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than previous week',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your household consumption of {previous_consumption,number,.#}lt the week before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_METER_WEEKLY_PREV_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than previous week',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your household consumption of {previous_consumption,number,.#}lt the week before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_METER_MONTHLY_PREV_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than previous month',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your household consumption of {previous_consumption,number,.#}lt the month before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_METER_MONTHLY_PREV_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than previous month',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your household consumption of {previous_consumption,number,.#}lt the month before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_METER_MONTHLY_PREV_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than previous month',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your household consumption of {previous_consumption,number,.#}lt the month before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_METER_MONTHLY_PREV_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than previous month',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your household consumption of {previous_consumption,number,.#}lt the month before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_SHOWER_WEEKLY_PREV_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than previous week',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your shower consumption of {previous_consumption,number,.#}lt the week before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_SHOWER_WEEKLY_PREV_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than previous week',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your shower consumption of {previous_consumption,number,.#}lt the week before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_SHOWER_WEEKLY_PREV_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than previous week',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your shower consumption of {previous_consumption,number,.#}lt the week before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_SHOWER_WEEKLY_PREV_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than previous week',
        'Last week you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your shower consumption of {previous_consumption,number,.#}lt the week before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_SHOWER_MONTHLY_PREV_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more than previous month',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your shower consumption of {previous_consumption,number,.#}lt the month before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_SHOWER_MONTHLY_PREV_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more than previous month',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% increase compared to your shower consumption of {previous_consumption,number,.#}lt the month before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_SHOWER_MONTHLY_PREV_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less than previous month',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your shower consumption of {previous_consumption,number,.#}lt the month before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B2_SHOWER_MONTHLY_PREV_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less than previous month',
        'Last month you spent {consumption,number,.#}lt, which is a {percent_change}% decrease compared to your shower consumption of {previous_consumption,number,.#}lt the month before that.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_METER_DAYOFWEEK_CONSUMPTION_PEAK'),
        'en',
        '{day,date,EEEE} is your peak day in household consumption',
        'You consume most water on {day,date,EEEE}. Specifically, you consume {consumption,number,.#}lt on {day,date,EEEE} while on average you consume {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_METER_DAYOFWEEK_CONSUMPTION_PEAK'),
        'es',
        '{day,date,EEEE} is your peak day in household consumption',
        'You consume most water on {day,date,EEEE}. Specifically, you consume {consumption,number,.#}lt on {day,date,EEEE} while on average you consume {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_METER_DAYOFWEEK_CONSUMPTION_LOW'),
        'en',
        '{day,date,EEEE} is your low day in household consumption',
        'You consume less water on {day,date,EEEE}. Specifically, you consume {consumption,number,.#}lt on {day,date,EEEE} while on average you consume {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_METER_DAYOFWEEK_CONSUMPTION_LOW'),
        'es',
        '{day,date,EEEE} is your low day in household consumption',
        'You consume less water on {day,date,EEEE}. Specifically, you consume {consumption,number,.#}lt on {day,date,EEEE} while on average you consume {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_SHOWER_DAYOFWEEK_CONSUMPTION_PEAK'),
        'en',
        '{day,date,EEEE} is your peak day in shower consumption',
        'You consume most water on {day,date,EEEE}. Specifically, you consume {consumption,number,.#}lt on {day,date,EEEE} while on average you consume {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_SHOWER_DAYOFWEEK_CONSUMPTION_PEAK'),
        'es',
        '{day,date,EEEE} is your peak day in shower consumption',
        'You consume most water on {day,date,EEEE}. Specifically, you consume {consumption,number,.#}lt on {day,date,EEEE} while on average you consume {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_SHOWER_DAYOFWEEK_CONSUMPTION_LOW'),
        'en',
        '{day,date,EEEE} is your low day in shower consumption',
        'You consume less water on {day,date,EEEE}. Specifically, you consume {consumption,number,.#}lt on {day,date,EEEE} while on average you consume {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B3_SHOWER_DAYOFWEEK_CONSUMPTION_LOW'),
        'es',
        '{day,date,EEEE} is your low day in shower consumption',
        'You consume less water on {day,date,EEEE}. Specifically, you consume {consumption,number,.#}lt on {day,date,EEEE} while on average you consume {average_consumption,number,.#}lt.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_METER_MORE_ON_WEEKEND'),
        'en',
        '{percent_change}% more water on weekends',
        'On average, your daily weekend consumption is {weekend_consumption,number,.#}lt vs. your daily weekday consumption of {weekday_consumption,number,.#}lt, considering your household.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_METER_MORE_ON_WEEKEND'),
        'es',
        '{percent_change}% more water on weekends',
        'On average, your daily weekend consumption is {weekend_consumption,number,.#}lt vs. your daily weekday consumption of {weekday_consumption,number,.#}lt, considering your household.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_METER_LESS_ON_WEEKEND'),
        'en',
        '{percent_change}% less water on weekends',
        'On average, your daily weekend consumption is {weekend_consumption,number,.#}lt vs. your daily weekday consumption of {weekday_consumption,number,.#}lt, considering your household.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_METER_LESS_ON_WEEKEND'),
        'es',
        '{percent_change}% less water on weekends',
        'On average, your daily weekend consumption is {weekend_consumption,number,.#}lt vs. your daily weekday consumption of {weekday_consumption,number,.#}lt, considering your household.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_SHOWER_MORE_ON_WEEKEND'),
        'en',
        '{percent_change}% more water on weekends',
        'On average, your daily weekend consumption is {weekend_consumption,number,.#}lt vs. your daily weekday consumption of {weekday_consumption,number,.#}lt, considering your shower.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_SHOWER_MORE_ON_WEEKEND'),
        'es',
        '{percent_change}% more water on weekends',
        'On average, your daily weekend consumption is {weekend_consumption,number,.#}lt vs. your daily weekday consumption of {weekday_consumption,number,.#}lt, considering your shower.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_SHOWER_LESS_ON_WEEKEND'),
        'en',
        '{percent_change}% less water on weekends',
        'On average, your daily weekend consumption is {weekend_consumption,number,.#}lt vs. your daily weekday consumption of {weekday_consumption,number,.#}lt, considering your shower.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B4_SHOWER_LESS_ON_WEEKEND'),
        'es',
        '{percent_change}% less water on weekends',
        'On average, your daily weekend consumption is {weekend_consumption,number,.#}lt vs. your daily weekday consumption of {weekday_consumption,number,.#}lt, considering your shower.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_METER_MONTHLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more water than same month last year',
        'Last month you spent {consumption,number,.#}lt while the same month last year you spent {previous_consumption,number,.#}lt, considering your household.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_METER_MONTHLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more water than same month last year',
        'Last month you spent {consumption,number,.#}lt while the same month last year you spent {previous_consumption,number,.#}lt, considering your household.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_METER_MONTHLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less water than same month last year',
        'Last month you spent {consumption,number,.#}lt while the same month last year you spent {previous_consumption,number,.#}lt, considering your household.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_METER_MONTHLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less water than same month last year',
        'Last month you spent {consumption,number,.#}lt while the same month last year you spent {previous_consumption,number,.#}lt, considering your household.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_SHOWER_MONTHLY_CONSUMPTION_INCR'),
        'en',
        '{percent_change}% more water than same month last year',
        'Last month you spent {consumption,number,.#}lt while the same month last year you spent {previous_consumption,number,.#}lt, considering your shower.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_SHOWER_MONTHLY_CONSUMPTION_INCR'),
        'es',
        '{percent_change}% more water than same month last year',
        'Last month you spent {consumption,number,.#}lt while the same month last year you spent {previous_consumption,number,.#}lt, considering your shower.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_SHOWER_MONTHLY_CONSUMPTION_DECR'),
        'en',
        '{percent_change}% less water than same month last year',
        'Last month you spent {consumption,number,.#}lt while the same month last year you spent {previous_consumption,number,.#}lt, considering your shower.'
    );

INSERT INTO public.recommendation_template_translation 
    (template, locale, title, description)
    VALUES
    (
        recommendation_template_from_name('INSIGHT_B5_SHOWER_MONTHLY_CONSUMPTION_DECR'),
        'es',
        '{percent_change}% less water than same month last year',
        'Last month you spent {consumption,number,.#}lt while the same month last year you spent {previous_consumption,number,.#}lt, considering your shower.'
    );

