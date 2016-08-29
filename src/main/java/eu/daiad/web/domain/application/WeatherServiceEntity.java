package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "weather_service")
@Table(schema = "public", name = "weather_service")
public class WeatherServiceEntity {

    @Id()
    @Column(name = "id")
    private int id;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private Set<WeatherServiceUtilityEntity> utilities = new HashSet<WeatherServiceUtilityEntity>();

    @Basic()
    private String name;

    @Basic()
    private String bean;

    @Basic()
    private boolean active;

    public String getBean() {
        return bean;
    }

    public boolean isActive() {
        return active;
    }

    @Basic()
    private String description;

    @Basic()
    private String endpoint;

    @Basic()
    private String website;

    @Column(name = "registered_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime registeredOn = new DateTime();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getWebsite() {
        return website;
    }

    public DateTime getRegisteredOn() {
        return registeredOn;
    }

    public int getId() {
        return id;
    }

    public Set<WeatherServiceUtilityEntity> getUtilities() {
        return utilities;
    }
}
