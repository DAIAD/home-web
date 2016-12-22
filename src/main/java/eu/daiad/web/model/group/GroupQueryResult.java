package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.domain.application.GroupEntity;

public class GroupQueryResult {

    private List<GroupEntity> groups;

    private int total;

    public List<GroupEntity> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupEntity> groups) {
        this.groups = groups;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
