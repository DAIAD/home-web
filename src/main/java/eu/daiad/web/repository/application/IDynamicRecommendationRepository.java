package eu.daiad.web.repository.application;

import java.util.Locale;

import eu.daiad.web.domain.application.DynamicRecommendationTranslationEntity;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;

public interface IDynamicRecommendationRepository
{
    DynamicRecommendationTranslationEntity findOne(EnumDynamicRecommendationType recommendationType, Locale locale);
}
