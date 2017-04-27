package eu.daiad.web.model.error;

public enum BudgetErrorCode implements ErrorCode {
    PARSE_ERROR,
    CREATION_FAILED,
    BUDGET_NOT_FOUND,
    BUDGET_SNAPSHOT_NOT_FOUND,
    BUDGET_ACCOUNT_NOT_FOUND;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
