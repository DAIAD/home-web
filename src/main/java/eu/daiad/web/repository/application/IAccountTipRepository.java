package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.Interval;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountTipEntity;
import eu.daiad.web.model.message.Tip;

public interface IAccountTipRepository
{
    AccountTipEntity findOne(int id);

    int countAll();

    AccountTipEntity findLastForAccount(UUID accountKey);

    List<AccountTipEntity> findByAccount(UUID accountKey);

    int countByAccount(UUID accountKey);

    int countByAccount(UUID accountKey, Interval interval);

    List<AccountTipEntity> findByAccount(UUID accountKey, Interval interval);

    List<AccountTipEntity> findByTip(int tipId);

    List<AccountTipEntity> findByTip(int tipId, Interval interval);

    int countByTip(int tipId);

    int countByTip(int tipId, Interval interval);

    List<AccountTipEntity> findByAccountAndTip(UUID accountKey, int tipId);

    List<AccountTipEntity> findByAccountAndTip(UUID accountKey, int tipId, Interval interval);

    int countByAccountAndTip(UUID accountKey, int tipId);

    int countByAccountAndTip(UUID accountKey, int tipId, Interval interval);

    AccountTipEntity create(AccountTipEntity e);

    AccountTipEntity createWith(UUID accountKey, int tipId);

    AccountTipEntity createWith(AccountEntity account, int tipId);

    Tip newMessage(int id);

    Tip newMessage(AccountTipEntity r);

    void delete(int id);

    void delete(AccountTipEntity e);
}
