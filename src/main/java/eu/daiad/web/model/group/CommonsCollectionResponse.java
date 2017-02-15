package eu.daiad.web.model.group;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.RestResponse;

public class CommonsCollectionResponse extends RestResponse {

    private List<CommonsInfo> groups;

    private int pageIndex = 0;

    private int pageSize = 0;

    private int count = 0;

    public CommonsCollectionResponse() {
        groups = new ArrayList<CommonsInfo>();
    }

    public CommonsCollectionResponse(CommonsInfo group) {
        groups = new ArrayList<CommonsInfo>();
        groups.add(group);

        if (group != null) {
            pageIndex = 0;
            pageSize = 1;
            count = 1;
        }
    }

    public CommonsCollectionResponse(List<CommonsInfo> groups) {
        this.groups = groups;

        if ((groups != null) && (!groups.isEmpty())) {
            pageIndex = 0;
            pageSize = groups.size();
            count = groups.size();
        }
    }

    public CommonsCollectionResponse(List<CommonsInfo> groups, int pageIndex, int pageSize, int count) {
        this.groups = groups;

        if ((groups != null) && (!groups.isEmpty())) {
            this.pageIndex = pageIndex;
            this.pageSize = pageSize;
            this.count = count;
        }
    }

    public CommonsCollectionResponse(String code, String description) {
        super(code, description);
    }

    public List<CommonsInfo> getGroups() {
        return groups;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getCount() {
        return count;
    }

}