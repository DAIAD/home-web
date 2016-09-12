package eu.daiad.web.model.error;

public enum MailErrorCode implements ErrorCode {
    SENT_FAILED;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + this.name());
    }
}