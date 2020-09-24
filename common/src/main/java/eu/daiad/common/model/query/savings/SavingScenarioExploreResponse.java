package eu.daiad.common.model.query.savings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.daiad.common.model.RestResponse;

public class SavingScenarioExploreResponse extends RestResponse {

    private UUID scenarioKey;

    private String scenarioName;

    private UUID clusterKey;

    private String clusterName;

    private List<SavingScenarioSegment> segments = new ArrayList<SavingScenarioSegment>();

    public SavingScenarioExploreResponse(SavingScenarioExploreResult result) {
        scenarioKey = result.getScenarioKey();
        scenarioName = result.getScenarioName();
        clusterKey = result.getClusterKey();
        clusterName = result.getClusterName();

        if (result.getSegments() != null) {
            segments = result.getSegments();
        }

    }

    public UUID getScenarioKey() {
        return scenarioKey;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public UUID getClusterKey() {
        return clusterKey;
    }

    public String getClusterName() {
        return clusterName;
    }

    public List<SavingScenarioSegment> getSegments() {
        return segments;
    }

}
