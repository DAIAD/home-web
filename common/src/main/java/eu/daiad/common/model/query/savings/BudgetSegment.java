package eu.daiad.common.model.query.savings;

import java.util.UUID;

import eu.daiad.common.domain.application.mappings.BudgetSegmentEntity;

public class BudgetSegment {

    private UUID key;

    private String name;

    private double consumptionBefore;

    private double consumptionAfter;

    public BudgetSegment(BudgetSegmentEntity entity) {
        key = entity.segmentKey;
        name = entity.segmentName;
        consumptionBefore = entity.consumptionBefore;
        consumptionAfter = entity.consumptionAfter;
    }

    public UUID getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public double getConsumptionBefore() {
        return consumptionBefore;
    }

    public double getConsumptionAfter() {
        return consumptionAfter;
    }

    public double getPercent() {
        if (consumptionBefore > 0) {
            return Math.round((consumptionBefore - consumptionAfter) * 10000 / consumptionBefore) / 100D;
        }
        return 0D;
    }

}
