package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name = "weather_service_utility_parameter")
@Table(schema = "public", name = "weather_service_utility_parameter")
public class WeatherServiceUtilityParameterEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "weather_service_utility_parameter_id_seq", name = "weather_service_utility_parameter_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "weather_service_utility_parameter_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @Basic()
    private String key;

    @Basic()
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

}
