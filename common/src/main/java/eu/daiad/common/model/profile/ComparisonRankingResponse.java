package eu.daiad.common.model.profile;

import eu.daiad.common.model.RestResponse;

public class ComparisonRankingResponse extends RestResponse {

    private ComparisonRanking comparison;

    public ComparisonRanking getComparison() {
        return comparison;
    }

    public void setComparison(ComparisonRanking comparison) {
        this.comparison = comparison;
    }

}
