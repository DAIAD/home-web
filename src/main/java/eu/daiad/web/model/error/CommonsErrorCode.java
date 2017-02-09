package eu.daiad.web.model.error;

public enum CommonsErrorCode implements ErrorCode {
    NAME_EXISTS,
    NOT_FOUND,
    OWNER_CANNOT_LEAVE_COMMONS;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
