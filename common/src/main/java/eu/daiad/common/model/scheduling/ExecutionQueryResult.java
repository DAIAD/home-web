package eu.daiad.common.model.scheduling;

import java.util.List;

import eu.daiad.common.domain.admin.ScheduledJobExecutionEntity;

public class ExecutionQueryResult {

    private int total;

    private List<ScheduledJobExecutionEntity> executions;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ScheduledJobExecutionEntity> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ScheduledJobExecutionEntity> executions) {
        this.executions = executions;
    }

}
