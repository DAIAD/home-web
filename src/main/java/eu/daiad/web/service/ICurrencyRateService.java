package eu.daiad.web.service;

import java.math.BigDecimal;
import java.util.Locale;

public interface ICurrencyRateService
{
    /**
     * Get currency-rate from a source to a target locale.
     *
     * @param source The source locale
     * @param target The target locale
     * @return the currency rate.
     */
    BigDecimal getRate(Locale source, Locale target);
}
