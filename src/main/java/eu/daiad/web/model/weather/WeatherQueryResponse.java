package eu.daiad.web.model.weather;

import java.util.List;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.service.weather.DailyWeatherData;
import eu.daiad.web.service.weather.HourlyWeatherData;

public class WeatherQueryResponse extends RestResponse {

    private List<DailyWeatherData> days;

    private List<HourlyWeatherData> hours;

    public List<DailyWeatherData> getDays() {
        return days;
    }

    public void setDays(List<DailyWeatherData> days) {
        this.days = days;
    }

    public List<HourlyWeatherData> getHours() {
        return hours;
    }

    public void setHours(List<HourlyWeatherData> hours) {
        this.hours = hours;
    }
}
