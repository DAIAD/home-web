package db.migration.daiad;

import javax.persistence.EntityManager;

import db.BaseMigration;
import eu.daiad.web.domain.application.AlertCodeEntity;
import eu.daiad.web.domain.application.AlertTypeEntity;
import eu.daiad.web.model.message.AlertCode;
import eu.daiad.web.model.message.EnumAlertType;

public class V1_0_67__LoadAlertCode extends BaseMigration
{
    @Override
    public void migrate(EntityManager em) throws RuntimeException
    {
        for (EnumAlertType type: EnumAlertType.values()) {
            AlertTypeEntity typeEntity = em.find(AlertTypeEntity.class, type.getValue());
            for (AlertCode code: type.getCodes()) {
                AlertCodeEntity codeEntity = new AlertCodeEntity(code, typeEntity);
                em.persist(codeEntity);
            }
        }
    }
}
