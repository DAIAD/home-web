package eu.daiad.web.domain.application;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import eu.daiad.web.model.message.AlertCode;
import eu.daiad.web.model.message.EnumAlertType;

@Entity(name = "alert_type")
@Table(schema = "public", name = "alert_type")
public class AlertTypeEntity
{
    @Id()
    private int value;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false)
    private EnumAlertType type;

    @Basic()
    private int priority;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "type")
    private List<AlertCodeEntity> codes = new ArrayList<>();

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public EnumAlertType getType() {
        return type;
    }

    public EnumAlertType asEnum() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public AlertTypeEntity()
    {
    }

    public AlertTypeEntity(EnumAlertType type)
    {
        this.type = type;
        this.value = type.getValue();
        this.priority = type.getPriority().intValue();
        for (AlertCode code: type.getCodes())
            this.codes.add(new AlertCodeEntity(code, this));
    }

    public List<AlertCodeEntity> getCodes()
    {
        return codes;
    }
}
