package eu.daiad.common.service.message;

import eu.daiad.common.model.message.Alert;

public abstract class AbstractAlertResolver 
    extends AbstractMessageResolver<Alert.ParameterizedTemplate>
    implements IAlertResolver
{
}
