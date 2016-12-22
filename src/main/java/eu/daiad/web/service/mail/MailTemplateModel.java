package eu.daiad.web.service.mail;

import java.util.HashMap;
import java.util.Map;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountProfileEntity;

/**
 * Helper class for passing data to mail templates
 */
public class MailTemplateModel {

    private AccountEntity account;

    private AccountProfileEntity profile;

    private Map<String, String> properties = new HashMap<String, String>();

    public MailTemplateModel(AccountEntity account, AccountProfileEntity profile) {
        this.account = account;
        this.profile = profile;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public AccountProfileEntity getProfile() {
        return profile;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

}
