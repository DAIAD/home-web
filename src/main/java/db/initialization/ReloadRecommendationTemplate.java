package db.initialization;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.util.Assert;

import db.BaseMigration;
import eu.daiad.web.domain.application.AccountRecommendationEntity;
import eu.daiad.web.domain.application.RecommendationTemplateEntity;
import eu.daiad.web.domain.application.RecommendationTypeEntity;
import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.EnumRecommendationType;

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
            Assert.state(typeEntity !=  null);
            RecommendationTemplateEntity templateEntity = new RecommendationTemplateEntity(template);
            templateEntity.setType(typeEntity);
            em.persist(templateEntity);    
        }
    }
}
