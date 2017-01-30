package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Locale;

import eu.daiad.web.domain.application.TipEntity;
import eu.daiad.web.model.message.Tip;

public interface ITipRepository
{
    TipEntity findOne(int index, Locale locale);

    TipEntity findOne(int id);

    List<TipEntity> findByLocale(Locale locale);

    TipEntity randomOne(Locale locale);

    List<TipEntity> random(Locale locale, int size);

    TipEntity create(TipEntity r);

    TipEntity createFrom(Tip tip);

    TipEntity updateFrom(TipEntity r, Tip tip);

    TipEntity saveFrom(Tip tip);

    void setActive(int id, boolean active);

    void setActive(TipEntity r, boolean active);

    void delete(int id);

    void delete(TipEntity r);

    Tip newMessage(int id);

    Tip newMessage(TipEntity r);
}
