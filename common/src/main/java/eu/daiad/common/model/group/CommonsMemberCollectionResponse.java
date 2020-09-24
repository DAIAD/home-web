package eu.daiad.common.model.group;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class CommonsMemberCollectionResponse extends RestResponse {

    private List<CommonsMemberInfo> members;

    private int pageIndex = 0;

    private int pageSize = 0;

    private int count = 0;

    public CommonsMemberCollectionResponse() {
        members = new ArrayList<CommonsMemberInfo>();
    }

    public CommonsMemberCollectionResponse(List<CommonsMemberInfo> members) {
        this.members = members;

        if ((members != null) && (!members.isEmpty())) {
            pageIndex = 0;
            pageSize = members.size();
            count = members.size();
        }
    }

    public CommonsMemberCollectionResponse(List<CommonsMemberInfo> members, int pageIndex, int pageSize, int count) {
        this.members = members;

        if ((members != null) && (!members.isEmpty())) {
            this.pageIndex = pageIndex;
            this.pageSize = pageSize;
            this.count = count;
        }
    }

    public CommonsMemberCollectionResponse(ErrorCode code, String description) {
        super(code, description);
    }

    public List<CommonsMemberInfo> getMembers() {
        return members;
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