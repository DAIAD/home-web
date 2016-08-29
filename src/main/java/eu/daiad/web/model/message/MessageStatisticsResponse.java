package eu.daiad.web.model.message;

import eu.daiad.web.model.RestResponse;
import java.util.List;

public class MessageStatisticsResponse extends RestResponse{
    
    private List<Alert> alertStatistics;
    private List<DynamicRecommendation> recommendationStatistics;

    public List<Alert> getAlertStatistics() {
        return alertStatistics;
    }

    public void setAlertStatistics(List<Alert> alertStatistics) {
        this.alertStatistics = alertStatistics;
    }

    public List<DynamicRecommendation> getRecommendationStatistics() {
        return recommendationStatistics;
    }

    public void setRecommendationStatistics(List<DynamicRecommendation> recommendationStatistics) {
        this.recommendationStatistics = recommendationStatistics;
    }
    
}
