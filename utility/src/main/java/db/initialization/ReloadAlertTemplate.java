package db.initialization;

import javax.persistence.EntityManager;

import org.springframework.util.Assert;

import db.BaseMigration;
import eu.daiad.common.domain.application.AlertTemplateEntity;
import eu.daiad.common.domain.application.AlertTypeEntity;
import eu.daiad.common.model.message.EnumAlertTemplate;
import eu.daiad.common.model.message.EnumAlertType;

public class ReloadAlertTemplate extends BaseMigration
{
    /**
     * Reload constants (enums) of {@code EnumAlertTemplate} into database.
     *
     * Every reference to alert templates will be dropped first: all existing alerts
     * and templates are deleted.
     */
    @Override
    public void migrate(EntityManager em) throws RuntimeException
    {
        // Delete existing alerts and templates

        em.createQuery("DELETE FROM account_alert_parameters").executeUpdate();

        em.createQuery("DELETE FROM account_alert").executeUpdate();

        em.createQuery("DELETE FROM alert_template_translation").executeUpdate();

        em.createQuery("DELETE FROM alert_template").executeUpdate();

        // Load templates from corresponding enumerated type

        for (EnumAlertTemplate template: EnumAlertTemplate.values()) {
            EnumAlertType type = template.getType();
            AlertTypeEntity typeEntity = em.find(AlertTypeEntity.class, type.getValue());
            Assert.state(typeEntity !=  null, "[Assertion failed] - Type entity not found");
            AlertTemplateEntity templateEntity = new AlertTemplateEntity(template);
            templateEntity.setType(typeEntity);
            em.persist(templateEntity);
        }
    }
}
