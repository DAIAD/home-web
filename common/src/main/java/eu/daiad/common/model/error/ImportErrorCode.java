package eu.daiad.common.model.error;

public enum ImportErrorCode implements ErrorCode {
    TOO_MANY_ERRORS;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
