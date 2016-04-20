package eu.daiad.web.model.profile;

import java.util.List;

public class ProfileModesFilterOptions {
	
	private List <String> groupName;
	private List <String> amphiro;
	private List <String> mobile;
	private List <String> social;
	
	
	public List<String> getGroupName() {
		return groupName;
	}
	public void setGroupName(List<String> groupName) {
		this.groupName = groupName;
	}
	public List<String> getAmphiro() {
		return amphiro;
	}
	public void setAmphiro(List<String> amphiro) {
		this.amphiro = amphiro;
	}
	public List<String> getMobile() {
		return mobile;
	}
	public void setMobile(List<String> mobile) {
		this.mobile = mobile;
	}
	public List<String> getSocial() {
		return social;
	}
	public void setSocial(List<String> social) {
		this.social = social;
	}
	
	
	
}