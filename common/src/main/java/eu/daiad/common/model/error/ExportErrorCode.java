package eu.daiad.common.model.error;

public enum ExportErrorCode implements ErrorCode {
    VALIDATION_RULE_VOLUME_LESS,
    VALIDATION_RULE_DURATION_LESS,
    VALIDATION_RULE_FLOW_LESS,
    VALIDATION_RULE_VOLUME_PERCENTILE,
    VALIDATION_RULE_TOO_MANY_RECORDS_REMOVED,
    ;

    @Override
    public String getMessageKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
