package eu.daiad.web.model.query.savings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.RestResponse;

public class BudgetExploreClusterResponse extends RestResponse {

    private UUID budgetKey;

    private String budgetName;

    private UUID clusterKey;

    private String clusterName;

    private List<BudgetSegment> segments = new ArrayList<BudgetSegment>();

    public BudgetExploreClusterResponse(BudgetExploreClusterResult result) {
        budgetKey = result.getBudgetKey();
        budgetName = result.getBudgetName();
        clusterKey = result.getClusterKey();
        clusterName = result.getClusterName();

        if (result.getSegments() != null) {
            segments = result.getSegments();
        }

    }

    public UUID getBudgetKey() {
        return budgetKey;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public UUID getClusterKey() {
        return clusterKey;
    }

    public String getClusterName() {
        return clusterName;
    }

    public List<BudgetSegment> getSegments() {
        return segments;
    }

}
