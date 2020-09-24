package eu.daiad.common.model.scheduling;

import java.util.List;

import eu.daiad.common.model.RestResponse;

public class ExecutionQueryResponse extends RestResponse {

    private int total;

    private int index;

    private int size;

    private List<JobExecutionInfo> executions;

    public List<JobExecutionInfo> getExecutions() {
        return executions;
    }

    public void setExecutions(List<JobExecutionInfo> executions) {
        this.executions = executions;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
