package eu.daiad.common.domain.application;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.daiad.common.model.message.EnumAlertTemplate;

@Entity(name = "alert_template")
@Table(
    schema = "public",
    name = "alert_template",
    indexes = {
        @Index(columnList = "name", unique = true),
    }
)
public class AlertTemplateEntity
{
    @Id()
    private int value;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    @NotNull
    private EnumAlertTemplate template;

    @ManyToOne()
    @JoinColumn(name = "type", nullable = false)
    @NotNull
    private AlertTypeEntity type = null;

    @OneToMany(
        mappedBy = "template",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<AlertTemplateTranslationEntity> translations = new HashSet<>();
    
    public EnumAlertTemplate getTemplate()
    {
        return template;
    }

    public EnumAlertTemplate asEnum()
    {
        return template;
    }

    public AlertTypeEntity getType()
    {
        return type;
    }

    public int getValue()
    {
        return value;
    }

    public void setType(AlertTypeEntity type)
    {
        this.type = type;
    }

    public AlertTemplateEntity()
    {}

    public AlertTemplateEntity(EnumAlertTemplate template)
    {
        this.template = template;
        this.value = template.getValue();
    }
    
    public Set<AlertTemplateTranslationEntity> getTranslations()
    {
        return translations;
    }
    
}
