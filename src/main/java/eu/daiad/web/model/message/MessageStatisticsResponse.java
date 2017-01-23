package eu.daiad.web.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.domain.application.AlertAnalyticsEntity;
import eu.daiad.web.model.RestResponse;

public class MessageStatisticsResponse extends RestResponse
{
    @JsonIgnore
    private List<AlertAnalyticsEntity> alertStats;

    @JsonIgnore
    private RecommendationStatistics recommendationStats;

    @JsonIgnore
    public RecommendationStatistics getRecommendationStats()
    {
        return recommendationStats;
    }

    @JsonIgnore
    public void setRecommendationStats(RecommendationStatistics stats)
    {
        this.recommendationStats = stats;
    }

    @JsonIgnore
    public List<AlertAnalyticsEntity> getAlertStats()
    {
        return alertStats;
    }

    @JsonIgnore
    public void setAlertStatistics(List<AlertAnalyticsEntity> stats)
    {
        this.alertStats = stats;
    }

    @JsonProperty("recommendationStatistics")
    public List<RecommendationStatistics.ByType> getRecommendationStatsAsList()
    {
        return recommendationStats.asList();
    }

    @JsonProperty("alertStatistics")
    public List<AlertAnalyticsEntity> getAlertStatsAsList()
    {
        // Todo Return a list as in JsonProprty("recommendationStatistics")
        return alertStats;
    }
}
