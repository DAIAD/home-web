package eu.daiad.common.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.common.model.RestResponse;

public class MessageStatisticsResponse extends RestResponse
{
    @JsonIgnore
    private AlertStatistics alertStats;

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
    public AlertStatistics getAlertStats()
    {
        return alertStats;
    }

    @JsonIgnore
    public void setAlertStatistics(AlertStatistics stats)
    {
        this.alertStats = stats;
    }

    @JsonProperty("recommendationStatistics")
    public List<RecommendationStatistics.ByType> getRecommendationStatsAsList()
    {
        return recommendationStats.asList();
    }

    @JsonProperty("alertStatistics")
    public List<AlertStatistics.ByType> getAlertStatsAsList()
    {
        return alertStats.asList();
    }
}
