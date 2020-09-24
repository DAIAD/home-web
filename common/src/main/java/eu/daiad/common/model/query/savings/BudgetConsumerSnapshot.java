package eu.daiad.common.model.query.savings;

import eu.daiad.common.domain.application.BudgetSnapshotAccountEntity;

public class BudgetConsumerSnapshot {
    private int year;

    private int month;

    private double consumptionBefore;

    private double consumptionAfter;

    public BudgetConsumerSnapshot(BudgetSnapshotAccountEntity consumerSnapshot) {
        year = consumerSnapshot.getSnapshot().getYear();
        month = consumerSnapshot.getSnapshot().getMonth();
        consumptionBefore = consumerSnapshot.getConsumptionBefore();
        consumptionAfter = consumerSnapshot.getConsumptionAfter();
    }

    public BudgetConsumerSnapshot(int year, int month, double consumptionBefore, double consumptionAfter) {
        this.year = year;
        this.month = month;
        this.consumptionBefore = consumptionBefore;
        this.consumptionAfter = consumptionAfter;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
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
