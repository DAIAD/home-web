package eu.daiad.web.model.scheduling;

import eu.daiad.web.domain.admin.EnumExecutionContainer;
import eu.daiad.web.domain.admin.EnumJobCategory;

public class JobInfo {

    private long id;

    private EnumJobCategory category;

    private EnumExecutionContainer container;

    private String name;

    private String description;

    private Long lastExecution;

    private Long lastExecutionDuration;

    private EnumExecutionExitCode lastExecutionExitCode;

    private String lastExecutionExitMessage;

    private Long nextExecution;

    private boolean enabled;

    private JobInfoSchedule schedule;

    private Float progress;

    private boolean running;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EnumJobCategory getCategory() {
        return category;
    }

    public void setCategory(EnumJobCategory category) {
        this.category = category;
    }

    public EnumExecutionContainer getContainer() {
        return container;
    }

    public void setContainer(EnumExecutionContainer container) {
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Long lastExecution) {
        this.lastExecution = lastExecution;
    }

    public Long getLastExecutionDuration() {
        return lastExecutionDuration;
    }

    public void setLastExecutionDuration(Long lastExecutionDuration) {
        this.lastExecutionDuration = lastExecutionDuration;
    }

    public EnumExecutionExitCode getLastExecutionExitCode() {
        return lastExecutionExitCode;
    }

    public void setLastExecutionExitCode(EnumExecutionExitCode lastExecutionExitCode) {
        this.lastExecutionExitCode = lastExecutionExitCode;
    }

    public String getLastExecutionExitMessage() {
        return lastExecutionExitMessage;
    }

    public void setLastExecutionExitMessage(String lastExecutionExitMessage) {
        this.lastExecutionExitMessage = lastExecutionExitMessage;
    }

    public Long getNextExecution() {
        return nextExecution;
    }

    public void setNextExecution(Long nextExecution) {
        this.nextExecution = nextExecution;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Float getProgress() {
        return progress;
    }

    public void setProgress(Float progress) {
        this.progress = progress;
    }

    public JobInfoSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(JobInfoSchedule schedule) {
        this.schedule = schedule;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
