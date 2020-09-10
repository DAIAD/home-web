package eu.daiad.web.repository.application;

import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AlertTemplateTranslationEntity;
import eu.daiad.web.model.message.EnumAlertTemplate;

@Repository
@Transactional
public class AlertTemplateTranslationRepository
    implements IAlertTemplateTranslationRepository
{
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public AlertTemplateTranslationEntity findByTemplate(EnumAlertTemplate template, Locale locale)
    {
        TypedQuery<AlertTemplateTranslationEntity> q = entityManager.createQuery(
            "SELECT t FROM alert_template_translation t WHERE " +
                "t.locale = :lang AND t.template.value = :template",
            AlertTemplateTranslationEntity.class);

        q.setParameter("lang", locale.getLanguage());
        q.setParameter("template", template.getValue());

        AlertTemplateTranslationEntity result;
        try {
            result = q.getSingleResult();
        } catch (NoResultException x) {
            result = null;
        }

        return result;
    }

    @Override
    public AlertTemplateTranslationEntity findByTemplate(EnumAlertTemplate template)
    {
        return findByTemplate(template, Locale.getDefault());
    }
}
