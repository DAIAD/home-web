package eu.daiad.common.model.query.savings;

import java.util.List;

import eu.daiad.common.model.RestResponse;

public class SavingScenarioQueryResponse extends RestResponse {

    private int total;

    private int pageIndex;

    private int pageSize;

    private List<SavingScenario> scenarios;

    public SavingScenarioQueryResponse(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        total = 0;
    }

    public SavingScenarioQueryResponse(int pageIndex, int pageSize, int total, List<SavingScenario> scenarios) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.total = total;
        this.scenarios = scenarios;
    }

    public SavingScenarioQueryResponse(SavingScenarioQueryResult result) {
        pageIndex = result.getPageIndex();
        pageSize = result.getPageSize();
        total = result.getTotal();
        scenarios = result.getScenarios();
    }

    public List<SavingScenario> getScenarios() {
        return scenarios;
    }

    public int getTotal() {
        return total;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

}
