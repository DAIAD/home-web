package eu.daiad.web.model.query.savings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BudgetExploreClusterResult {

    private UUID budgetKey;

    private String budgetName;

    private UUID clusterKey;

    private String clusterName;

    private List<BudgetSegment> segments = new ArrayList<BudgetSegment>();

    public UUID getBudgetKey() {
        return budgetKey;
    }

    public void setBudgetKey(UUID budgetKey) {
        this.budgetKey = budgetKey;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public UUID getClusterKey() {
        return clusterKey;
    }

    public void setClusterKey(UUID clusterKey) {
        this.clusterKey = clusterKey;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<BudgetSegment> getSegments() {
        return segments;
    }

}
