package eu.daiad.web.repository.application;

import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.RecommendationTemplateTranslationEntity;
import eu.daiad.web.model.message.EnumRecommendationTemplate;

@Repository
@Transactional
public class RecommendationTemplateTranslationRepository
    implements IRecommendationTemplateTranslationRepository
{
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public RecommendationTemplateTranslationEntity findByTemplate(
        EnumRecommendationTemplate template, Locale locale)
    {
        TypedQuery<RecommendationTemplateTranslationEntity> q = entityManager.createQuery(
            "SELECT t FROM recommendation_template_translation t WHERE " +
                "t.locale = :lang AND t.template.value = :template",
            RecommendationTemplateTranslationEntity.class);

        q.setParameter("lang", locale.getLanguage());
        q.setParameter("template", template.getValue());

        RecommendationTemplateTranslationEntity result;
        try {
            result = q.getSingleResult();
        } catch (NoResultException x) {
            result = null;
        }

        return result;
    }

    @Override
    public RecommendationTemplateTranslationEntity findByTemplate(
        EnumRecommendationTemplate template)
    {
        return findByTemplate(template, Locale.getDefault());
    }
}
