package eu.daiad.web.annotate.message;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface GenerateMessages
{
    String period() default "P1D";
    
    int maxPerWeek() default 5;
    
    int maxPerMonth() default 15;
}
