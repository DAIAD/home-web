package db.initialization;

import java.util.EnumSet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.util.Assert;

import db.BaseMigration;
import eu.daiad.common.domain.application.RecommendationTemplateEntity;
import eu.daiad.common.domain.application.RecommendationTypeEntity;
import eu.daiad.common.model.message.EnumRecommendationTemplate;
import eu.daiad.common.model.message.EnumRecommendationType;

/**
 * Load constants (enums) of {@code EnumRecommendationTemplate} into database.
 */
public class LoadRecommendationTemplate extends BaseMigration
{
    @Override
    public void migrate(EntityManager em) throws RuntimeException
    {
        TypedQuery<RecommendationTemplateEntity> q =
            em.createQuery("FROM recommendation_template", RecommendationTemplateEntity.class);
        
        // Check constants already mapped as entities
        
        EnumSet<EnumRecommendationTemplate> found = EnumSet.noneOf(EnumRecommendationTemplate.class);
        for (RecommendationTemplateEntity templateEntity: q.getResultList()) {
            EnumRecommendationTemplate template = templateEntity.getTemplate();
            RecommendationTypeEntity typeEntity = templateEntity.getType(); 
            Assert.state(
                template != null && 
                template.getValue() == templateEntity.getValue() && 
                template.getType() == typeEntity.getType(), 
                "[Assertion failed] - Database is inconsistent"
            );
            found.add(template);
        }
        
        // Insert missing constants
        
        for (EnumRecommendationTemplate template: EnumSet.complementOf(found)) {
            EnumRecommendationType type = template.getType();
            RecommendationTypeEntity typeEntity = em.find(RecommendationTypeEntity.class, type.getValue());
            Assert.state(typeEntity !=  null, "[Assertion failed] - Database is inconsistent");
            RecommendationTemplateEntity templateEntity = new RecommendationTemplateEntity(template);
            templateEntity.setType(typeEntity);
            em.persist(templateEntity);    
        }
    }
}
