package eu.daiad.common.service;

public interface IEnergyCalculator
{
    /**
     * Compute energy needed to rise the temperature of water for @param degrees.
     * 
     * @param degrees (Celsius)
     * @param volume (litres)
     * @return energy needed (Kwh) 
     */
    double computeEnergyToRiseTemperature(double degrees, double volume);
}
