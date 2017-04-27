package eu.daiad.web.model.query.savings;

import java.util.List;

public class SavingScenarioQueryResult {

    private Integer pageIndex = 0;

    private Integer pageSize = 10;

    int total = 0;

    private List<SavingScenario> scenarios = null;

    public SavingScenarioQueryResult(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public SavingScenarioQueryResult(int pageIndex, int pageSize, int total, List<SavingScenario> scenarios) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;

        this.total = total;
        this.scenarios = scenarios;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public int getTotal() {
        return total;
    }

    public List<SavingScenario> getScenarios() {
        return scenarios;
    }

}
