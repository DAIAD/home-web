package eu.daiad.web.model.user;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class UserInfoCollectionResponse extends RestResponse {

    private List<UserInfo> users;

    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

}
