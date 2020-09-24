package eu.daiad.common.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class PriceDataService implements IPriceDataService
{    
    @Override
    public double getPricePerKwh(String countryName)
    {
        double price = 0.0;
        
        if (countryName == null)
            return price;
        
        // Express prices in a common application-wide currency (euro)
        
        switch (countryName) {
        case "United Kingdom":
            price = 0.177;
            break;
        case "Spain":
            price = 0.224;
            break;
        case "Greece":
        default:
            price = 0.224;
            break;
        }
        return price;
    }

    @Override
    public double getPricePerKwh(Locale locale)
    {
        return getPricePerKwh(locale.getCountry());
    }

}
