package eu.daiad.web.repository.application;

import java.util.Locale;

import eu.daiad.web.domain.application.RecommendationTemplateTranslationEntity;
import eu.daiad.web.model.message.EnumRecommendationTemplate;

public interface IRecommendationTemplateTranslationRepository
{
    RecommendationTemplateTranslationEntity findByTemplate(EnumRecommendationTemplate template, Locale locale);
    
    RecommendationTemplateTranslationEntity findByTemplate(EnumRecommendationTemplate template);
}
