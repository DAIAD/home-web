package eu.daiad.web.model.error;

public enum WeatherErrorCode implements ErrorCode {
    SERVICE_NOT_FOUND,
    UTILITY_NOT_FOUND,
    INVALID_DATETIME;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + this.name());
    }
}
