package eu.daiad.web.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class PriceDataService implements IPriceDataService
{    
    @Override
    public double getPricePerKwh(Locale locale)
    {
        double price;
        
        // All prices are expressed in euros
        
        switch (locale.getCountry()) {
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

}
