package eu.daiad.common.model.error;

public enum PasswordErrorCode implements ErrorCode {
    WEAK_PASSWORD;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
