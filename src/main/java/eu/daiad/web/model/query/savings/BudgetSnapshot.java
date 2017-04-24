package eu.daiad.web.model.query.savings;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.daiad.web.domain.application.BudgetSnapshotEntity;

public class BudgetSnapshot {

    private UUID budgetKey;

    private UUID utilityKey;

    private int year;

    private int month;

    private Double consumptionBefore;

    private Double consumptionAfter;

    private Double savingsPercent;

    private DateTime createdOn;

    public BudgetSnapshot(BudgetSnapshotEntity entity) {
        budgetKey = entity.getBudget().getKey();
        utilityKey = entity.getBudget().getUtility().getKey();
        year = entity.getYear();
        month = entity.getMonth();
        consumptionBefore = entity.getConsumptionBefore();
        consumptionAfter = entity.getConsumptionAfter();
        savingsPercent = entity.getPercent();
        createdOn = entity.getCreatedOn();
    }

    public UUID getBudgetKey() {
        return budgetKey;
    }

    public UUID getUtilityKey() {
        return utilityKey;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public Double getConsumptionBefore() {
        return consumptionBefore;
    }

    public Double getConsumptionAfter() {
        return consumptionAfter;
    }

    public Double getSavingsPercent() {
        return savingsPercent;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

}
