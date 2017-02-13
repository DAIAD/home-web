package eu.daiad.web.service;

import java.util.Locale;

public interface IPriceDataService
{
    /** 
     * Get an (average) price of 1 KWh for a particular locale 
     * 
     * @return price (using the default currency of the application)
     * */
    double getPricePerKwh(Locale locale);
}
