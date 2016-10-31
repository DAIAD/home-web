package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name = "weather_service_utility")
@Table(schema = "public", name = "weather_service_utility")
public class WeatherServiceUtilityEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "weather_service_utility_id_seq", name = "weather_service_utility_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "weather_service_utility_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utility_id", nullable = false)
    private UtilityEntity utility;

    @ManyToOne()
    @JoinColumn(name = "service_id", nullable = false)
    private WeatherServiceEntity service;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_utility_id")
    private Set<WeatherServiceUtilityParameterEntity> parameters = new HashSet<WeatherServiceUtilityParameterEntity>();

    public int getId() {
        return id;
    }

    public UtilityEntity getUtility() {
        return utility;
    }

    public WeatherServiceEntity getService() {
        return service;
    }

    public Set<WeatherServiceUtilityParameterEntity> getParameters() {
        return parameters;
    }

}
