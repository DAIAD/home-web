package eu.daiad.common.model.weather;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HourlyWeatherData {

    private Double temperature;

    private Double temperatureFeel;

    private Double precipitation;

    private Double humidity;

    private Double windSpeed;

    private String windDirection;

    private String conditions;

    private String datetime;

    public HourlyWeatherData(String datetime) {
        this.datetime = datetime;
    }
    
    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public Double getTemperatureFeel() {
        return temperatureFeel;
    }

    public void setTemperatureFeel(Double temperatureFeel) {
        this.temperatureFeel = temperatureFeel;
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

    public String getDatetime() {
        return datetime;
    }

    @JsonIgnore
    public String getDate() {
        return StringUtils.substring(this.datetime, 0, 8);
    }

    @JsonIgnore
    public String getHour() {
        return StringUtils.substring(this.datetime, 8, 10);
    }

}
