package eu.daiad.web.model.scheduling;

import java.util.List;

import eu.daiad.web.domain.admin.ScheduledJobExecution;

public class ExecutionQueryResult {

    private int total;

    private List<ScheduledJobExecution> executions;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ScheduledJobExecution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ScheduledJobExecution> executions) {
        this.executions = executions;
    }

}
