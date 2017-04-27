package db.initialization;

import java.util.EnumSet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.util.Assert;

import db.BaseMigration;
import eu.daiad.web.domain.application.AlertTypeEntity;
import eu.daiad.web.model.message.EnumAlertType;

public class LoadAlertType extends BaseMigration
{
    /**
     * Load constants (enums) of {@code EnumAlertType} into database.
     */
    @Override
    public void migrate(EntityManager em) throws RuntimeException
    {
        TypedQuery<AlertTypeEntity> q =
            em.createQuery("FROM alert_type", AlertTypeEntity.class);

        // Check constants already mapped as entities

        EnumSet<EnumAlertType> found = EnumSet.noneOf(EnumAlertType.class);
        for (AlertTypeEntity e: q.getResultList()) {
            EnumAlertType t = e.getType();
            Assert.state(t != null && t.getValue() == e.getValue());
            found.add(t);
        }

        // Insert missing constants

        for (EnumAlertType t: EnumSet.complementOf(found)) {
            AlertTypeEntity e = new AlertTypeEntity(t);
            em.persist(e);
        }
    }

}
