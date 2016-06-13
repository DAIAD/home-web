package eu.daiad.web.model.user;

import java.util.List;

import eu.daiad.web.domain.application.Account;

public class UserQueryResult {

    private List<Account> accounts;

    private int total;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
