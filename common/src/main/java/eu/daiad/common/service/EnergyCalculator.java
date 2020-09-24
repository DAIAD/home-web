package eu.daiad.common.service;

import org.springframework.stereotype.Service;

@Service
public class EnergyCalculator implements IEnergyCalculator
{
    // 1 kcal is 1.163E-3 kWh
    public static final double KWH_PER_KCAL = 1.163E-3;
    
    @Override
    public double computeEnergyToRiseTemperature(double degrees, double volume)
    {
        // Formula: <degrees> * <litres> * <kwh-per-kcal>
        // http://antoine.frostburg.edu/chem/senese/101/thermo/faq/energy-required-for-temperature-rise.shtml
        // https://answers.yahoo.com/question/index?qid=20071209205616AADfWQ3

        // 1 calorie will raise the temperature of 1 gram of water 1 degree Celsius.
        // 1000 calories will raise the temperature of 1 litre of water 1 degree Celsius
        
        
        // Example:
        // 2 degrees, total 30 showers per month, 40 liters per shower.
        // (2 C) * (12 * 30 * 40 lit) * (1.163*10^-3 Kwh/kcal) = ..
        
        return (degrees * volume *  KWH_PER_KCAL);
    }

}
