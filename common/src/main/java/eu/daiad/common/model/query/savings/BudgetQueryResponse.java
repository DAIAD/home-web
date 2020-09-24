package eu.daiad.common.model.query.savings;

import java.util.List;

import eu.daiad.common.model.RestResponse;

public class BudgetQueryResponse extends RestResponse {

    private int total;

    private int pageIndex;

    private int pageSize;

    private List<Budget> budgets;

    public BudgetQueryResponse(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        total = 0;
    }

    public BudgetQueryResponse(int pageIndex, int pageSize, int total, List<Budget> budgets) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.total = total;
        this.budgets = budgets;
    }

    public BudgetQueryResponse(BudgetQueryResult result) {
        pageIndex = result.getPageIndex();
        pageSize = result.getPageSize();
        total = result.getTotal();
        budgets = result.getBudgets();
    }

    public List<Budget> getBudgets() {
        return budgets;
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
