package eu.daiad.web.domain.application;

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

import eu.daiad.web.model.message.EnumRecommendationTemplate;

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

    @OneToMany(
        mappedBy = "template",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<RecommendationTemplateTranslationEntity> translations = new HashSet<>(); 
    
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
    
    public Set<RecommendationTemplateTranslationEntity> getTranslations()
    {
        return translations;
    }
}
