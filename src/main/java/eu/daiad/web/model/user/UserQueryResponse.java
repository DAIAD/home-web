package eu.daiad.web.model.user;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class UserQueryResponse extends RestResponse {

    private int total = 0;

    private int index;

    private int size;

    private List<UserInfo> accounts;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<UserInfo> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<UserInfo> accounts) {
        this.accounts = accounts;
    }

}
