package eu.daiad.web.model.scheduling;

public class JobInfoScheduleCron extends JobInfoSchedule {

    private String cronExpression;

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public EnumScheduleType getType() {
        return EnumScheduleType.CRON;
    }

}
