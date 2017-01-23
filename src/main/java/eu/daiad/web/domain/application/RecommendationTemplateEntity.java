package eu.daiad.web.domain.application;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.EnumRecommendationType;

@Entity(name = "recommendation_template")
@Table(
    schema = "public",
    name = "recommendation_template",
    indexes = {
        @Index(columnList = "name", unique = true),
    }
)
public class RecommendationTemplateEntity
{
    @Id()
    private int value;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    @NotNull
    private EnumRecommendationTemplate template;
    
    @ManyToOne()
    @JoinColumn(name = "type", nullable = false)
    @NotNull
    private RecommendationTypeEntity type = null;
    
    public EnumRecommendationTemplate getTemplate()
    {
        return template;
    }
    
    public EnumRecommendationTemplate asEnum()
    {
        return template;
    }
    
    public RecommendationTypeEntity getType()
    {
        return type;
    }
    
    public int getValue()
    {
        return value;
    }
    
    public EnumRecommendationType getEnumeratedType()
    {
        return type.getType();
    }
    
    public void setType(RecommendationTypeEntity type)
    {
        this.type = type;
    }
 
    public RecommendationTemplateEntity()
    {}
    
    public RecommendationTemplateEntity(EnumRecommendationTemplate template)
    {
        this.template = template;
        this.value = template.getValue();
    }
    
}
