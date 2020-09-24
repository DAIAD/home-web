package eu.daiad.common.model.query.savings;

import java.util.UUID;

import eu.daiad.common.domain.application.mappings.SavingScenarioSegmentEntity;

public class SavingScenarioSegment {

    private UUID key;

    private String name;

    private double consumption;

    private double potential;

    public SavingScenarioSegment(SavingScenarioSegmentEntity entity) {
        key = entity.segmentKey;
        name = entity.segmentName;
        consumption = entity.consumption;
        potential = entity.potential;
    }

    public UUID getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public double getConsumption() {
        return consumption;
    }

    public double getPotential() {
        return potential;
    }

    public double getPercent() {
        if ((consumption > 0) && (consumption > potential)) {
            return Math.round(potential * 10000 / consumption) / 100D;
        }
        return 0D;
    }
}
