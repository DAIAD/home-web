package db.initialization;

import javax.persistence.EntityManager;

import org.springframework.util.Assert;

import db.BaseMigration;
import eu.daiad.common.domain.application.RecommendationTemplateEntity;
import eu.daiad.common.domain.application.RecommendationTypeEntity;
import eu.daiad.common.model.message.EnumRecommendationTemplate;
import eu.daiad.common.model.message.EnumRecommendationType;

public class ReloadRecommendationTemplate extends BaseMigration
{
    /**
     * Reload constants (enums) of {@code EnumRecommendationTemplate} into database.
     *
     * Every reference to recommendation templates will be dropped first: all existing
     * recommendations and templates are deleted.
     */
    @Override
    public void migrate(EntityManager em) throws RuntimeException
    {
        // Delete existing recommendations and templates

        em.createQuery("DELETE FROM account_recommendation_parameters").executeUpdate();

        em.createQuery("DELETE FROM account_recommendation").executeUpdate();

        em.createQuery("DELETE FROM recommendation_template_translation").executeUpdate();

        em.createQuery("DELETE FROM recommendation_template").executeUpdate();

        // Load templates from corresponding enumerated type

        for (EnumRecommendationTemplate template: EnumRecommendationTemplate.values()) {
            EnumRecommendationType type = template.getType();
            RecommendationTypeEntity typeEntity = em.find(RecommendationTypeEntity.class, type.getValue());
            Assert.state(typeEntity !=  null, "[Assertion failed] - Type entity not found");
            RecommendationTemplateEntity templateEntity = new RecommendationTemplateEntity(template);
            templateEntity.setType(typeEntity);
            em.persist(templateEntity);
        }
    }
}
