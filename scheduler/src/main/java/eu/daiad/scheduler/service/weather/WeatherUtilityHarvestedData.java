package eu.daiad.scheduler.service.weather;

import java.util.List;

import org.joda.time.DateTime;

import eu.daiad.common.model.weather.DailyWeatherData;

public class WeatherUtilityHarvestedData {

    private int utilityId;

    private DateTime createdOn;

    private List<DailyWeatherData> data;

    public WeatherUtilityHarvestedData(int utilityId) {
        this.utilityId = utilityId;
    }

    public int getUtilityId() {
        return utilityId;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public List<DailyWeatherData> getData() {
        return data;
    }

    public void setData(List<DailyWeatherData> data) {
        this.data = data;
    }

}
