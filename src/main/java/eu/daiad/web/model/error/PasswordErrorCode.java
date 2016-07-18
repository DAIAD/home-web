package eu.daiad.web.model.error;

public enum PasswordErrorCode implements ErrorCode {
    INVALID_LENGTH;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + this.name());
    }
}
