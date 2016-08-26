package eu.daiad.web.model.message;

import eu.daiad.web.model.RestResponse;
import java.util.Map;

public class MessageStatisticsResponse extends RestResponse{
    
    private Map<Alert, Integer> alertStatistics;
    private Map<DynamicRecommendation, Integer> recommendationStatistics;

    public Map<Alert, Integer> getAlertStatistics() {
        return alertStatistics;
    }

    public void setAlertStatistics(Map<Alert, Integer> alertStatistics) {
        this.alertStatistics = alertStatistics;
    }

    public Map<DynamicRecommendation, Integer> getRecommendationStatistics() {
        return recommendationStatistics;
    }

    public void setRecommendationStatistics(Map<DynamicRecommendation, Integer> recommendationStatistics) {
        this.recommendationStatistics = recommendationStatistics;
    }
    
}
