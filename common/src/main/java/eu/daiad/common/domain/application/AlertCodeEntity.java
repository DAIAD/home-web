package eu.daiad.common.domain.application;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.daiad.common.model.message.AlertCode;

@Entity(name = "alert_code")
@Table(schema = "public", name = "alert_code")
public class AlertCodeEntity
{
    @Id()
    private String code;

    @ManyToOne()
    @JoinColumn(name = "type", nullable = false, updatable = false)
    @NotNull
    private AlertTypeEntity type;

    protected AlertCodeEntity() {

    }

    public AlertCodeEntity(AlertCode code, AlertTypeEntity typeEntity)
    {
        this.code = code.toString();
        this.type = typeEntity;
    }

    public String getCode()
    {
        return code;
    }

    public AlertTypeEntity getType()
    {
        return type;
    }
}
