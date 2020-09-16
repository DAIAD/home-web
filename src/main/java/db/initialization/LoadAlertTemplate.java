package db.initialization;

import java.util.EnumSet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.util.Assert;

import db.BaseMigration;
import eu.daiad.web.domain.application.AlertTemplateEntity;
import eu.daiad.web.domain.application.AlertTypeEntity;
import eu.daiad.web.model.message.EnumAlertTemplate;
import eu.daiad.web.model.message.EnumAlertType;

/**
 * Load constants (enums) of {@code EnumAlertTemplate} into database.
 */
public class LoadAlertTemplate extends BaseMigration
{
    @Override
    public void migrate(EntityManager em) throws RuntimeException
    {
        TypedQuery<AlertTemplateEntity> q =
            em.createQuery("FROM alert_template", AlertTemplateEntity.class);

        // Check constants already mapped as entities

        EnumSet<EnumAlertTemplate> found = EnumSet.noneOf(EnumAlertTemplate.class);
        for (AlertTemplateEntity templateEntity: q.getResultList()) {
            EnumAlertTemplate template = templateEntity.getTemplate();
            AlertTypeEntity typeEntity = templateEntity.getType();
            Assert.state(
                template != null &&
                template.getValue() == templateEntity.getValue() &&
                template.getType() == typeEntity.getType(), 
                "[Assertion failed] - Database is inconsistent"
            );
            found.add(template);
        }

        // Insert missing constants

        for (EnumAlertTemplate template: EnumSet.complementOf(found)) {
            EnumAlertType type = template.getType();
            AlertTypeEntity typeEntity = em.find(AlertTypeEntity.class, type.getValue());
            Assert.state(typeEntity !=  null, "[Assertion failed] - Database is inconsistent");
            AlertTemplateEntity templateEntity = new AlertTemplateEntity(template);
            templateEntity.setType(typeEntity);
            em.persist(templateEntity);
        }
    }

}
