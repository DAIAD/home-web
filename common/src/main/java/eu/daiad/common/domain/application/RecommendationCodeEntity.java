package eu.daiad.common.domain.application;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.daiad.common.model.message.RecommendationCode;

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

    protected RecommendationCodeEntity() {

    }

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
