package db.initialization;

import java.util.Arrays;
import java.util.EnumSet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang.StringUtils;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.springframework.util.Assert;

import db.BaseMigration;
import eu.daiad.web.domain.application.RecommendationTypeEntity;
import eu.daiad.web.model.EnumToNameTransformer;
import eu.daiad.web.model.message.EnumRecommendationType;

public class LoadRecommendationType extends BaseMigration
{
    /**
     * Load constants (enums) of {@code EnumRecommendationType} into database.
     */
    @Override
    public void migrate(EntityManager em) throws RuntimeException
    {        
        TypedQuery<RecommendationTypeEntity> q = 
            em.createQuery("FROM recommendation_type", RecommendationTypeEntity.class);
        
        // Check constants already mapped as entities
        
        EnumSet<EnumRecommendationType> found = EnumSet.noneOf(EnumRecommendationType.class);
        for (RecommendationTypeEntity e: q.getResultList()) {
            EnumRecommendationType t = e.getType();
            Assert.state(t != null && t.getValue() == e.getValue());
            found.add(t);
        }
        
        // Insert missing constants
        
        for (EnumRecommendationType t: EnumSet.complementOf(found)) {
            RecommendationTypeEntity e = new RecommendationTypeEntity(t);
            em.persist(e);    
        }
    }
}
