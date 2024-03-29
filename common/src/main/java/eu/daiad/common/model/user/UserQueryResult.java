package eu.daiad.common.model.user;

import java.util.List;

import eu.daiad.common.domain.application.AccountEntity;

public class UserQueryResult {

    private List<AccountEntity> accounts;

    private int total;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
