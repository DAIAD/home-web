package eu.daiad.web.model.scheduling;

public class JobInfoSchedulePeriodic extends JobInfoSchedule {

    private Long period;

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    @Override
    public EnumScheduleType getType() {
        return EnumScheduleType.PERIOD;
    }

}
