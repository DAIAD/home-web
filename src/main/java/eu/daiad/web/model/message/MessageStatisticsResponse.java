package eu.daiad.web.model.message;

import java.util.List;

import eu.daiad.web.domain.application.AlertAnalyticsEntity;
import eu.daiad.web.domain.application.RecommendationAnalyticsEntity;
import eu.daiad.web.model.RestResponse;

public class MessageStatisticsResponse extends RestResponse
{
    private List<AlertAnalyticsEntity> alertStatistics;
    private List<RecommendationAnalyticsEntity> recommendationStatistics;

    public List<AlertAnalyticsEntity> getAlertStatistics() {
        return alertStatistics;
    }

    public void setAlertStatistics(List<AlertAnalyticsEntity> alertStatistics) {
        this.alertStatistics = alertStatistics;
    }

    public List<RecommendationAnalyticsEntity> getRecommendationStatistics() {
        return recommendationStatistics;
    }

    public void setRecommendationStatistics(List<RecommendationAnalyticsEntity> recommendationStatistics) {
        this.recommendationStatistics = recommendationStatistics;
    }

}
