package db.migration.daiad;

import javax.persistence.EntityManager;

import db.BaseMigration;
import eu.daiad.common.domain.application.RecommendationCodeEntity;
import eu.daiad.common.domain.application.RecommendationTypeEntity;
import eu.daiad.common.model.message.EnumRecommendationType;
import eu.daiad.common.model.message.RecommendationCode;

public class V1_0_68__LoadRecommendationCode extends BaseMigration
{
    @Override
    public void migrate(EntityManager em) throws RuntimeException
    {
        for (EnumRecommendationType type: EnumRecommendationType.values()) {
            RecommendationTypeEntity typeEntity = em.find(RecommendationTypeEntity.class, type.getValue());
            for (RecommendationCode code: type.getCodes()) {
                RecommendationCodeEntity codeEntity = new RecommendationCodeEntity(code, typeEntity);
                em.persist(codeEntity);
            }
        }
    }
}
