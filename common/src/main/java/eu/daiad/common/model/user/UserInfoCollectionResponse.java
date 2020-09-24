package eu.daiad.common.model.user;

import java.util.List;

import eu.daiad.common.model.RestResponse;

public class UserInfoCollectionResponse extends RestResponse {

    private List<UserInfo> users;

    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

}
