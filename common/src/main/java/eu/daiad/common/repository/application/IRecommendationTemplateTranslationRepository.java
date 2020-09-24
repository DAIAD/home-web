package eu.daiad.common.repository.application;

import java.util.Locale;

import eu.daiad.common.domain.application.RecommendationTemplateTranslationEntity;
import eu.daiad.common.model.message.EnumRecommendationTemplate;

public interface IRecommendationTemplateTranslationRepository
{
    RecommendationTemplateTranslationEntity findByTemplate(EnumRecommendationTemplate template, Locale locale);
    
    RecommendationTemplateTranslationEntity findByTemplate(EnumRecommendationTemplate template);
}
