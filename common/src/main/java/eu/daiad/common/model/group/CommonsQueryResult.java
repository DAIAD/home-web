package eu.daiad.common.model.group;

import java.util.ArrayList;
import java.util.List;

public class CommonsQueryResult {

    private List<CommonsInfo> groups = new ArrayList<CommonsInfo>();

    private int count = 0;

    private int pageIndex = 0;

    private int pageSize = 0;

    public CommonsQueryResult() { }

    public CommonsQueryResult(int pageIndex, int pageSize, int count) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.count = count;
    }

    public List<CommonsInfo> getGroups() {
        return groups;
    }

    public int getCount() {
        return count;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

}
