package eu.daiad.web.domain.application;

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

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "weather_data_day")
@Table(schema = "public", name = "weather_data_day")
public class WeatherDailyDataEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "weather_data_day_id_seq", name = "weather_data_day_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "weather_data_day_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "utility_id", nullable = false)
    private UtilityEntity utility;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "service_id", nullable = false)
    private WeatherServiceEntity service;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "min_temperature")
    private Double minTemperature;

    @Column(name = "max_temperature")
    private Double maxTemperature;

    @Column(name = "min_temperature_feel")
    private Double minTemperatureFeel;

    @Column(name = "max_temperature_feel")
    private Double maxTemperatureFeel;

    @Column()
    private Double precipitation;

    @Column(name = "min_humidity")
    private Double minHumidity;

    @Column(name = "max_humidity")
    private Double maxHumidity;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "wind_direction")
    private String windDirection;

    @Basic()
    private String conditions;

    @Column(name = "date", columnDefinition = "bpchar", length = 10)
    private String date;

    public UtilityEntity getUtility() {
        return utility;
    }

    public void setUtility(UtilityEntity utility) {
        this.utility = utility;
    }

    public WeatherServiceEntity getService() {
        return service;
    }

    public void setService(WeatherServiceEntity service) {
        this.service = service;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getId() {
        return id;
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

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

}
