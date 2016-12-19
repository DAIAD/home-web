package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Locale;

import eu.daiad.web.domain.application.StaticRecommendationEntity;

public interface IStaticRecommendationRepository
{
    StaticRecommendationEntity findOne(int recommendationId, Locale locale);
    
    StaticRecommendationEntity randomOne(Locale locale);
    
    List<StaticRecommendationEntity> random(Locale locale, int size);
}
