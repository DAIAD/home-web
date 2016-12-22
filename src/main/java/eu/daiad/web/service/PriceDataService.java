package eu.daiad.web.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class PriceDataService implements IPriceDataService
{
    private static final double ENERGY_EUROS_PER_KWH = 0.224;
    private static final double ENERGY_GBP_PER_KWH = 0.15;
    
    private double VOLUME_EUROS_PER_LITRE = 0.0024;
    
    @Override
    public double getPricePerKwh(Locale locale)
    {
        double price;
        switch (locale.getCountry()) {
        case "United Kingdom":
            price = ENERGY_GBP_PER_KWH;
            break;
        case "Spain":
        case "Greece":
        default:
            price = ENERGY_EUROS_PER_KWH;
            break;
        }
        return price;
    }

}
