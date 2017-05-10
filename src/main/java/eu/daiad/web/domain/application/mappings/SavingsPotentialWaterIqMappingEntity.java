package eu.daiad.web.domain.application.mappings;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

public class SavingsPotentialWaterIqMappingEntity {

    public SavingsPotentialWaterIqMappingEntity(String serial, String iq, UUID userKey) {
        this.serial = serial;
        this.iq = iq;
        this.userKey = userKey;
    }

    @Id()
    @Basic()
    public String serial;

    @Basic()
    public String iq;

    @Column(name = "user_key")
    @Type(type = "pg-uuid")
    public UUID userKey;

    @Transient
    public int value;

}
