package eu.daiad.web.model.scheduling;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ExecutionQuery {

    private Integer index = 0;

    private Integer size = 10;

    @JsonDeserialize(using = EnumExecutionExitCode.Deserializer.class)
    private EnumExecutionExitCode exitCode = EnumExecutionExitCode.UNDEFINED;

    private String jobName;

    private Long startDate;

    private Long endDate;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public EnumExecutionExitCode getExitCode() {
        if(exitCode==null) {
            return EnumExecutionExitCode.UNDEFINED;
        }
        return exitCode;
    }

    public void setExitCode(EnumExecutionExitCode exitCode) {
        this.exitCode = exitCode;
    }

}
