package eu.daiad.web.repository.application;

import java.util.Locale;

import eu.daiad.web.domain.application.AlertTemplateTranslationEntity;
import eu.daiad.web.model.message.EnumAlertTemplate;

public interface IAlertTemplateTranslationRepository
{
    AlertTemplateTranslationEntity findByTemplate(EnumAlertTemplate template, Locale locale);

    AlertTemplateTranslationEntity findByTemplate(EnumAlertTemplate template);
}
