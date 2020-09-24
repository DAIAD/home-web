package eu.daiad.common.domain.application;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity(name = "weather_data_hour")
@Table(schema = "public", name = "weather_data_hour")
public class WeatherHourlyDataEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "weather_data_hour_id_seq", name = "weather_data_hour_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "weather_data_hour_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "day_id", nullable = false)
    private WeatherDailyDataEntity day;

    @Column()
    private Double temperature;

    @Column(name = "temperature_feel")
    private Double temperatureFeel;

    @Column()
    private Double precipitation;

    @Column()
    private Double humidity;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "wind_direction")
    private String windDirection;

    @Basic()
    private String conditions;

    @Column(name = "datetime", columnDefinition = "bpchar", length = 10)
    private String datetime;

    public WeatherDailyDataEntity getDay() {
        return day;
    }

    public void setDay(WeatherDailyDataEntity day) {
        this.day = day;
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

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Double getTemperatureFeel() {
        return temperatureFeel;
    }

    public void setTemperatureFeel(Double temperatureFeel) {
        this.temperatureFeel = temperatureFeel;
    }

    public long getId() {
        return id;
    }

    public String getDate() {
        return StringUtils.substring(this.datetime, 0, 8);
    }

    public String getHour() {
        return StringUtils.substring(this.datetime, 8, 10);
    }
}
