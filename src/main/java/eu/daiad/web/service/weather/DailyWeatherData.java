package eu.daiad.web.service.weather;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DailyWeatherData {

    @JsonIgnore
    private List<HourlyWeatherData> hours = new ArrayList<HourlyWeatherData>();

    private String date;

    private Double minTemperature;

    private Double maxTemperature;

    private Double minTemperatureFeel;

    private Double maxTemperatureFeel;

    private Double precipitation;

    private Double minHumidity;

    private Double maxHumidity;

    private Double windSpeed;

    private String windDirection;

    private String conditions;

    public DailyWeatherData(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public List<HourlyWeatherData> getHours() {
        return hours;
    }

    public Double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(Double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public Double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(Double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public Double getMinTemperatureFeel() {
        return minTemperatureFeel;
    }

    public void setMinTemperatureFeel(Double minTemperatureFeel) {
        this.minTemperatureFeel = minTemperatureFeel;
    }

    public Double getMaxTemperatureFeel() {
        return maxTemperatureFeel;
    }

    public void setMaxTemperatureFeel(Double maxTemperatureFeel) {
        this.maxTemperatureFeel = maxTemperatureFeel;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }

    public Double getMinHumidity() {
        return minHumidity;
    }

    public void setMinHumidity(Double minHumidity) {
        this.minHumidity = minHumidity;
    }

    public Double getMaxHumidity() {
        return maxHumidity;
    }

    public void setMaxHumidity(Double maxHumidity) {
        this.maxHumidity = maxHumidity;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public HourlyWeatherData getHour(String datetime) {
        for (HourlyWeatherData item : this.hours) {
            if (item.getDatetime().equals(datetime)) {
                return item;
            }
        }

        HourlyWeatherData item = new HourlyWeatherData(datetime);

        this.hours.add(item);

        return item;

    }
}
