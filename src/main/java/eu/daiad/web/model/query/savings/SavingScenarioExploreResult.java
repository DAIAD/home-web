package eu.daiad.web.model.query.savings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SavingScenarioExploreResult {

    private UUID scenarioKey;

    private String scenarioName;

    private UUID clusterKey;

    private String clusterName;

    private List<SavingScenarioSegment> segments = new ArrayList<SavingScenarioSegment>();

    public UUID getScenarioKey() {
        return scenarioKey;
    }

    public void setScenarioKey(UUID scenarioKey) {
        this.scenarioKey = scenarioKey;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
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

    public List<SavingScenarioSegment> getSegments() {
        return segments;
    }

}
