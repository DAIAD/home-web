TRUNCATE TABLE public.alert_template_translation;

--
-- Load public.alert_template_translation
-- 
 
INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_LEAK'),
        'en',
        'Check for water leaks!',
        'We believe there could be a water leak in your house. Please check all fixtures for leaks and contact your water utility.'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_LEAK'),
        'es',
        '¡Comprueba las fugas de agua!',
        'Creemos que tienes una fuga de agua en casa. Por favor, comprueba tus dispositivos y contacta con tu compañía suministradora de agua.'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('SHOWER_ON'),
        'en',
        'Shower still on!',
        'Someone forgot to close the shower?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('SHOWER_ON'),
        'es',
        '¡Te has dejado la ducha encendida!',
        '¿Alguien ha olvidado cerrar el grifo de la ducha?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_FIXTURES'),
        'en',
        'Check the water fixtures!',
        'Please check your water fixtures, there may be water running in one of them!'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_FIXTURES'),
        'es',
        'Comprueba tus dispositivos que funcionen con agua',
        'Por favor, comprueba tus dispositivos que funcionen con agua, puede que te hayas dejado alguno funcionando.'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('UNUSUAL_ACTIVITY'),
        'en',
        'Unusual activity detected!',
        'Water is being used in your household at an unusual time. Is everything OK?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('UNUSUAL_ACTIVITY'),
        'es',
        '¡Actividad inusual detectada!',
        'Se está utilizando agua en una hora un poco inusual. ¿Está todo bien?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_QUALITY'),
        'en',
        'Water quality not assured!',
        'Remember to leave the water running for a few minutes when you return home after a long period of absence. Temperatures have been over 28 since you last used water. Better be safe from Legionella.'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_QUALITY'),
        'es',
        '¡Calidad del agua no asegurada!',
        'Recuerda dejar correr un par de minutos el agua en el grifo cuando vuelvas a casa después de estar mucho tiempo fuera. Si la temperatura ha superado los 28ºC en algún momento podrías contraer legionela.'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('HIGH_TEMPERATURE'),
        'en',
        'Water too hot!',
        'Be careful, the water in your shower is extremely hot!'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('HIGH_TEMPERATURE'),
        'es',
        '¡Agua demasiado caliente!',
        '¡Cuidado, el agua de tu ducha está demasiado caliente!'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('NEAR_DAILY_WATER_BUDGET'),
        'en',
        'Reached {percent_threshold}% of your daily water budget',
        'You have already used <h1>{consumption,number,.#}</h1> litres, and you have <h1>{remaining,number,.#}</h1> litres remaining for today. Want some tips to save water?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('NEAR_DAILY_WATER_BUDGET'),
        'es',
        'Ya has consumido el {percent_threshold}% de lo que sueles consumir normalmente al día.',
        'Ya has consumo <h1>{consumption,number,.#}</h1> litros, y normalmente gastas <h1>{remaining,number,.#}</h1> al día. ¿Quieres algunas recomendaciones para ahorrar agua?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('NEAR_WEEKLY_WATER_BUDGET'),
        'en',
        'Reached {percent_threshold}% of your weekly water budget',
        'You have already used <h1>{consumption,number,.#}</h1> litres, and you have <h1>{remaining,number,.#}</h1> litres remaining for this week. Want some tips to save water?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('NEAR_WEEKLY_WATER_BUDGET'),
        'es',
        'Ya has consumido el {percent_threshold}% de lo que sueles consumir normalmente a la semana',
        'Ya has consumo <h1>{consumption,number,.#}</h1> litros, y normalmente gastas <h1>{remaining,number,.#}</h1> a la semana. ¿Quieres algunas recomendaciones para ahorrar agua?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('NEAR_DAILY_SHOWER_BUDGET'),
        'en',
        'Reached {percent_threshold}% of your daily shower budget',
        'You have already used <h1>{consumption,number,.#}</h1> litres, and you have <h1>{remaining,number,.#}</h1> litres remaining for today. Want some tips to save water?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('NEAR_DAILY_SHOWER_BUDGET'),
        'es',
        'Ya has consumido el {percent_threshold}% de lo que sueles consumir normalmente al día en la ducha.',
        'Ya has consumo <h1>{consumption,number,.#}</h1> litros, y normalmente gastas <h1>{remaining,number,.#}</h1> al día duchándote. ¿Quieres algunas recomendaciones para ahorrar agua?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('NEAR_WEEKLY_SHOWER_BUDGET'),
        'en',
        'Reached {percent_threshold}% of your weekly shower budget',
        'You have already used <h1>{consumption,number,.#}</h1> litres and you have <h1>{remaining,number,.#}</h1> litres remaining for this week. Want some tips to save water?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('NEAR_WEEKLY_SHOWER_BUDGET'),
        'es',
        'Ya has consumido el {percent_threshold}% de lo que sueles consumir normalmente a la semana en la ducha',
        'Ya has consumo <h1>{consumption,number,.#}</h1> litros, y normalmente gastas <h1>{remaining,number,.#}</h1> a la semana duchándote. ¿Quieres algunas recomendaciones para ahorrar agua?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('REACHED_DAILY_WATER_BUDGET'),
        'en',
        'Reached daily Water Budget',
        'You have used all <h1>{budget,number,integer}</h1> litres of your water budget. Let’s stick to our budget tomorrow! Want some tips to save water?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('REACHED_DAILY_WATER_BUDGET'),
        'es',
        'Alcanzado el consumo medio diario de agua',
        'Has consumo los <h1>{budget,number,integer}</h1> litros que consumes normalmente. ¡Intentemos mejorarlo mañana! ¿Quieres algunas recomendaciones para ahorrar agua?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('REACHED_DAILY_SHOWER_BUDGET'),
        'en',
        'Reached daily Shower Budget',
        'You have used all <h1>{budget,number,integer}</h1> litres of your shower budget. Let’s stick to our budget tomorrow! Want some tips to save water?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('REACHED_DAILY_SHOWER_BUDGET'),
        'es',
        'Alcanzado el consumo medio diario de agua en la ducha',
        'Has consumo los <h1>{budget,number,integer}</h1> litros que consumes normalmente en la ducha. ¡Intentemos mejorarlo mañana! ¿Quieres algunas recomendaciones para ahorrar agua?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_CHAMPION'),
        'en',
        'You are a real water champion!',
        'Well done! You have managed to stay within your budget for 1 whole month! Feel you can do better? Try targeting a slightly lower daily budget.'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_CHAMPION'),
        'es',
        '¡Eres un genio del agua!',
        '¡Bien hecho! ¡Has conseguido mejorar tu consumo diario durante 1 mes completo! ¿Podrías hacerlo mejor? Intenta optimizar un poco más tu consumo.'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('SHOWER_CHAMPION'),
        'en',
        'You are a real shower champion!',
        'Well done! You have managed to stay within your shower budget for 1 whole month! Feel you can do better? Try targeting a slightly lower daily shower budget.'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('SHOWER_CHAMPION'),
        'es',
        '¡Eres un genio de la ducha!',
        '¡Bien hecho! ¡Has conseguido mejorar tu consumo diario en la ducha durante 1 mes completo! ¿Podrías hacerlo mejor? Intenta optimizar un poco más tu consumo'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOO_MUCH_WATER_METER'),
        'en',
        'You are using too much water',
        'You are using twice the amount of water compared to city average. You could save up to <h1>{annual_savings,number,integer}</h1> liters. Want to learn how?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOO_MUCH_WATER_METER'),
        'es',
        '¡Estás gastando mucha agua!',
        'Estás gastando el doble que la media ciudadana. Puedes ahorrar sobre <h1>{annual_savings,number,integer}</h1> litros. ¿Quiéres aprender cómo?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOO_MUCH_WATER_SHOWER'),
        'en',
        'You are using too much water in the shower',
        'You are using twice the amount of water compared to other consumers. You could save up to <h1>{annual_savings,number,integer}</h1> liters. Want to learn how?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOO_MUCH_WATER_SHOWER'),
        'es',
        '¡Estás gastando mucha agua en la ducha!',
        'Estás consumiendo el doble que otros. Puedes ahorrar sobre <h1>{annual_savings,number,integer}</h1> litros. ¿Quiéres aprender cómo?'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOO_MUCH_ENERGY'),
        'en',
        'You are spending too much energy for showering',
        'You are spending too much hot water in the shower. Reducing the water temperature by a few degrees could save you up to <h1>{money1,number,currency}</h1> a year. Want to learn how?'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOO_MUCH_ENERGY'),
        'es',
        'Estás gastando mucha energía en tu ducha',
        'Estás gastando mucha agua caliente. Reduce la temperatura un par de grados, podrías ahorrar <h1>{money1,number,currency}</h1>. ¿Quiéres aprender cómo’'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('REDUCED_WATER_USE_METER'),
        'en',
        'Well done! You have greatly reduced your water use',
        'You have reduced your water use by <h1>{percent_change}</h1>% since you joined DAIAD! Congratulations, you are a water champion!'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('REDUCED_WATER_USE_METER'),
        'es',
        '¡Bien hecho! Has reducido tu consumo de agu',
        '¡Has reducido tu consum de agua en un <h1>{percent_change}</h1>% desde que participas en el piloto DAIAD!'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('REDUCED_WATER_USE_SHOWER'),
        'en',
        'Well done! You have greatly improved your shower efficiency',
        'You have reduced your water use in the shower by <h1>{percent_change}</h1>% since you joined DAIAD! Congratulations, you are a water champion!'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('REDUCED_WATER_USE_SHOWER'),
        'es',
        '¡Bien hecho! Has mejorado la eficiencia de tus duchas',
        '¡Has reducido tu consumo de agua en la ducha un <h1>{percent_change}</h1>% desde que participas en el piloto DAIAD! ¡Enhorabuena, eres un genio del agua!'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_EFFICIENCY_LEADER'),
        'en',
        'Congratulations! You are a water efficiency leader',
        'You are a true leader for water efficiency! If everyone adopted your behavior your city could save <h1>{annual_savings}</h1> litres of water'
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('WATER_EFFICIENCY_LEADER'),
        'es',
        '¡Enhorabuena! ¡Eres un máquina ahorrando agua!',
        '¡Eres realmente un máquina ahorrando agua! Si todos se comportaran como tú la ciudad ahorraría <h1>{annual_savings}</h1> litros de agua.'
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('KEEP_UP_SAVING_WATER'),
        'en',
        'Keep up saving water!',
        ''
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('KEEP_UP_SAVING_WATER'),
        'es',
        '¡Ahorremos agua!',
        ''
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('GOOD_JOB_MONTHLY'),
        'en',
        'You are doing a great job!',
        ''
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('GOOD_JOB_MONTHLY'),
        'es',
        '¡Buen trabajo!',
        ''
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('LITERS_ALREADY_SAVED'),
        'en',
        'You have already saved <h1>{weekly_savings}</h1> litres of water!',
        ''
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('LITERS_ALREADY_SAVED'),
        'es',
        '¡Vamos, ya has ahorrado <h1>{weekly_savings}</h1> litros!',
        ''
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOP_25_PERCENT_OF_SAVERS'),
        'en',
        'Congratulations! You are one of the top 25% savers in your region.',
        ''
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOP_25_PERCENT_OF_SAVERS'),
        'es',
        '¡Enhorabuena! Estás en el top 25 del piloto!',
        ''
    ); 


INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOP_10_PERCENT_OF_SAVERS'),
        'en',
        'Congratulations! You are among the top group of savers in your city.',
        ''
    ); 

INSERT INTO public.alert_template_translation 
    (template, locale, title, description)
    VALUES
    (
        alert_template_from_name('TOP_10_PERCENT_OF_SAVERS'),
        'es',
        '¡Enhorabuena! Estás entre los que más ahorran del piloto!',
        ''
    ); 

