package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.domain.application.Group;

public class GroupQueryResult {

    private List<Group> groups;

    private int total;

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
