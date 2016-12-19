package eu.daiad.web.repository.application;

import java.util.Locale;

import eu.daiad.web.domain.application.AlertEntity;
import eu.daiad.web.domain.application.AlertTranslationEntity;
import eu.daiad.web.model.message.EnumAlertType;

public interface IAlertRepository
{
    AlertEntity findOne(EnumAlertType alertType);
}
