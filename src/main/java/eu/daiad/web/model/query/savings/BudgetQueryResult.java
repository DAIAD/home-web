package eu.daiad.web.model.query.savings;

import java.util.List;

public class BudgetQueryResult {

    private Integer pageIndex = 0;

    private Integer pageSize = 10;

    int total = 0;

    private List<Budget> budgets = null;

    public BudgetQueryResult(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public BudgetQueryResult(int pageIndex, int pageSize, int total, List<Budget> budgets) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;

        this.total = total;
        this.budgets = budgets;
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

    public List<Budget> getBudgets() {
        return budgets;
    }

}
