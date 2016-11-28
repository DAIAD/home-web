package eu.daiad.web.model.profile;

import eu.daiad.web.model.RestResponse;

public class ComparisonRankingResponse extends RestResponse {

    private ComparisonRanking comparison;

    public ComparisonRanking getComparison() {
        return comparison;
    }

    public void setComparison(ComparisonRanking comparison) {
        this.comparison = comparison;
    }

}
