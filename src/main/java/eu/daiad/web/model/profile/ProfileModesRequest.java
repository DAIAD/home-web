package eu.daiad.web.model.profile;

public class ProfileModesRequest {
	
	private String nameFilter;
	private String groupName;
	private String amphiro;
	private String mobile;
	private String social;

	public String getNameFilter() {
		return nameFilter;
	}

	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getAmphiro() {
		return amphiro;
	}

	public void setAmphiro(String amphiro) {
		this.amphiro = amphiro;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getSocial() {
		return social;
	}

	public void setSocial(String social) {
		this.social = social;
	}
}