package eu.daiad.web.model.error;

public enum ImportErrorCode implements ErrorCode {
    TOO_MANY_ERRORS;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
