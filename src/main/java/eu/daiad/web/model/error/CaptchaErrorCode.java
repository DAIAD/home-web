package eu.daiad.web.model.error;

public enum CaptchaErrorCode implements ErrorCode {
    CAPTCHA_SERVICE_ERROR,
    CAPTCHA_VERIFICATION_ERROR;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
