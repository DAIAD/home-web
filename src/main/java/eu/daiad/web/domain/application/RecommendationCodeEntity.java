package eu.daiad.web.domain.application;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.daiad.web.model.message.RecommendationCode;

@Entity(name = "recommendation_code")
@Table(schema = "public", name = "recommendation_code")
public class RecommendationCodeEntity
{
    @Id()
    private String code;

    @ManyToOne()
    @JoinColumn(name = "type", nullable = false, updatable = false)
    @NotNull
    private RecommendationTypeEntity type;

    private RecommendationCodeEntity() {}

    public RecommendationCodeEntity(RecommendationCode code, RecommendationTypeEntity typeEntity)
    {
        this.code = code.toString();
        this.type = typeEntity;
    }

    public String getCode()
    {
        return code;
    }

    public RecommendationTypeEntity getType()
    {
        return type;
    }
}
