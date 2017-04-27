package eu.daiad.web.domain.application.mappings;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

public class BudgetConsumerEntity {

    public BudgetConsumerEntity(int id, String userName, UUID userKey, double consumptionBefore, double consumptionAfter) {
        this.id = id;
        this.userName = userName;
        this.userKey = userKey;
        this.consumptionBefore = consumptionBefore;
        this.consumptionAfter = consumptionAfter;
    }

    @Id()
    @Basic()
    public int id;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "user_key")
    @Type(type = "pg-uuid")
    public UUID userKey;

    @Column(name = "consumption_before")
    public double consumptionBefore;

    @Column(name = "consumption_after")
    public double consumptionAfter;

}
