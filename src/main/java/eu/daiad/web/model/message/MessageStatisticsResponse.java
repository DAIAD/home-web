package eu.daiad.web.model.message;

import java.util.List;

import eu.daiad.web.domain.application.AlertAnalytics;
import eu.daiad.web.domain.application.RecommendationAnalytics;
import eu.daiad.web.model.RestResponse;

public class MessageStatisticsResponse extends RestResponse{
    
    private List<AlertAnalytics> alertStatistics;
    private List<RecommendationAnalytics> recommendationStatistics;

    public List<AlertAnalytics> getAlertStatistics() {
        return alertStatistics;
    }

    public void setAlertStatistics(List<AlertAnalytics> alertStatistics) {
        this.alertStatistics = alertStatistics;
    }

    public List<RecommendationAnalytics> getRecommendationStatistics() {
        return recommendationStatistics;
    }

    public void setRecommendationStatistics(List<RecommendationAnalytics> recommendationStatistics) {
        this.recommendationStatistics = recommendationStatistics;
    }
    
}
