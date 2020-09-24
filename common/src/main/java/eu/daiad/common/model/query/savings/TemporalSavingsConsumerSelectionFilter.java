package eu.daiad.common.model.query.savings;

public class TemporalSavingsConsumerSelectionFilter extends SavingsConsumerSelectionFilter {

    private SavingsTimeFilter time;

    public SavingsTimeFilter getTime() {
        return time;
    }

    public void setTime(SavingsTimeFilter time) {
        this.time = time;
    }

}
