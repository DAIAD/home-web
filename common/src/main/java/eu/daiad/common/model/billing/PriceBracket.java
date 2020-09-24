package eu.daiad.common.model.billing;

public class PriceBracket {

    private double minVolume;

    private Double maxVolume;

    private double price;

    public PriceBracket(double minVolume, Double maxVolume, double price) {
        this.minVolume = minVolume;
        this.maxVolume = maxVolume;
        this.price = price;
    }

    public PriceBracket(double minVolume, double price) {
        this.minVolume = minVolume;
        this.price = price;
    }

    public double getMinVolume() {
        return minVolume;
    }

    public Double getMaxVolume() {
        return maxVolume;
    }

    public double getPrice() {
        return price;
    }

}
