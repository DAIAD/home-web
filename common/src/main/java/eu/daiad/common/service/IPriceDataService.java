package eu.daiad.common.service;

import java.util.Locale;

public interface IPriceDataService
{
    /**
     * Get an (average) price of 1 KWh for a particular locale
     *
     * @return price (using the default currency of the application)
     * */
    double getPricePerKwh(Locale locale);

    /**
     * Get an (average) price of 1 KWh for a particular country
     *
     * @param countryName
     * @return average price of 1 KWh.
     */
    double getPricePerKwh(String countryName);
}
