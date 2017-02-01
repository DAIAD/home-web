package eu.daiad.web.service;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class DefaultCurrencyRateService
    implements ICurrencyRateService
{
    @Override
    public BigDecimal getRate(Locale source, Locale target)
    {
        // Todo Return actual currency rate
        return BigDecimal.ONE;
    }
}
