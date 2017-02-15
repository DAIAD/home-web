package eu.daiad.web.model.group;

import java.util.ArrayList;
import java.util.List;

public class CommonsMemberQueryResult {

    private List<CommonsMemberInfo> members = new ArrayList<CommonsMemberInfo>();

    private int count = 0;

    private int pageIndex = 0;

    private int pageSize = 0;

    public CommonsMemberQueryResult() {
    }

    public CommonsMemberQueryResult(int pageIndex, int pageSize, int count) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.count = count;
    }

    public List<CommonsMemberInfo> getMembers() {
        return members;
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
