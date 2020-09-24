package eu.daiad.common.repository.application;

import java.util.Locale;

import eu.daiad.common.domain.application.AlertTemplateTranslationEntity;
import eu.daiad.common.model.message.EnumAlertTemplate;

public interface IAlertTemplateTranslationRepository
{
    AlertTemplateTranslationEntity findByTemplate(EnumAlertTemplate template, Locale locale);

    AlertTemplateTranslationEntity findByTemplate(EnumAlertTemplate template);
}
