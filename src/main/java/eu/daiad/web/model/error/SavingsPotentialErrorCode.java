package eu.daiad.web.model.error;

public enum SavingsPotentialErrorCode implements ErrorCode {
    PARSE_ERROR,
    CREATION_FAILED,
    SCENARIO_NOT_FOUND,
    SCENARIO_ACCOUNT_NOT_FOUND;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
