package eu.daiad.web.annotate.message;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import eu.daiad.web.model.EnumDayOfWeek;

@Retention(RUNTIME)
@Target(TYPE)
public @interface MessageGenerator
{
    /**
     * A period at which this generator should normally run.
     */
    String period() default "P1D";
    
    /**
     * Specify the day(s) of week on which we should run.
     * 
     * Note: If you specify a day, ensure that Spring's scheduler fires the parent
     * job at least on a daily basis! 
     */    
    EnumDayOfWeek[] dayOfWeek() default {};
    
    /**
     * Specify the day(s) of month (1-31) on which we should run.
     * 
     * Note: If you specify a day, ensure that Spring's scheduler fires the parent
     * job at least on a daily basis! 
     */
    int[] dayOfMonth() default {};
    
    /**
     * Specify a per-account weekly (rough) limit for the number of generated messages.
     */
    int maxPerWeek() default 5;
    
    /**
     * Specify a per-account monthly (rough) limit for the number of generated messages.
     */
    int maxPerMonth() default 15;
}
