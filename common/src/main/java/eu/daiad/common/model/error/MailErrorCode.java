package eu.daiad.common.model.error;

public enum MailErrorCode implements ErrorCode {
    LOG_FAILED,
    SENT_FAILED;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}