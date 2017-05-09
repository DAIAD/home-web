package eu.daiad.web.model.query.savings;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class BudgetParameters {

    private Scenario scenario;

    private Integer goal;

    @JsonDeserialize(using = EnumBudgetDistribution.Deserializer.class)
    private EnumBudgetDistribution distribution;

    private SavingsConsumerSelectionFilter include = new SavingsConsumerSelectionFilter();

    private SavingsConsumerSelectionFilter exclude = new SavingsConsumerSelectionFilter();

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Integer getGoal() {
        return goal;
    }

    public void setGoal(Integer goal) {
        this.goal = goal;
    }

    public EnumBudgetDistribution getDistribution() {
        if (distribution == null) {
            return EnumBudgetDistribution.UNDEFINED;
        }
        return distribution;
    }

    public void setDistribution(EnumBudgetDistribution distribution) {
        this.distribution = distribution;
    }

    public SavingsConsumerSelectionFilter getInclude() {
        return include;
    }

    public void setInclude(SavingsConsumerSelectionFilter include) {
        this.include = include;
    }

    public SavingsConsumerSelectionFilter getExclude() {
        return exclude;
    }

    public void setExclude(SavingsConsumerSelectionFilter exclude) {
        this.exclude = exclude;
    }

    public static class Scenario {

        private UUID key;

        private int percent;

        public UUID getKey() {
            return key;
        }

        public void setKey(UUID key) {
            this.key = key;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

    }

}