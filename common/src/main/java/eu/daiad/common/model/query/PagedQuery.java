package eu.daiad.common.model.query;

import eu.daiad.common.model.AuthenticatedRequest;

public class PagedQuery extends AuthenticatedRequest {

	private int pageIndex;

	private int pageSize;

	public int getPageIndex() {
		if (this.pageIndex < 0) {
			return 0;
		}
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		if (this.pageSize < 0) {
			return 10;
		}
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
