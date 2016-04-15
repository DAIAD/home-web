package eu.daiad.web.model.profile;

import java.util.UUID;
import eu.daiad.web.model.EnumApplication;

public class ProfileModes {
	
	public enum AmphiroModeState{
		NOT_APPLICABLE(0),
		OFF(1),
		ON(2);
			    
		private int amphiroModeState;

		AmphiroModeState(int state){
			amphiroModeState = state;
	    }
			    
	    public int getAmphiroModeState(){
	    	return amphiroModeState;
	    }
	}
	
	public enum MobileModeState{
		NOT_APPLICABLE(0),
		OFF(1),
		ON(2);
			    
		private int mobileModeState;

		MobileModeState(int state){
			mobileModeState = state;
	    }
			    
	    public int getMobileModeState(){
	    	return mobileModeState;
	    }
	}
	
	public enum SocialModeState{
		NOT_APPLICABLE(0),
		OFF(1),
		ON(2);
			    
		private int socialModeState;

		SocialModeState(int state){
			socialModeState = state;
	    }
			    
	    public int getSocialModeState(){
	    	return socialModeState;
	    }
	}

	
	private UUID id = null;
	
	private String name = null;

	private MobileModeState mobile = MobileModeState.NOT_APPLICABLE;
	
	private AmphiroModeState amphiro = AmphiroModeState.NOT_APPLICABLE;
	
	private SocialModeState social = SocialModeState.NOT_APPLICABLE;
	
	private int groupId = -1;
	
	private String groupName = null;
	
	private boolean active = false;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MobileModeState getMobile() {
		return mobile;
	}
	public void setMobile(MobileModeState mobile) {
		this.mobile = mobile;
	}

	public AmphiroModeState getAmphiro() {
		return amphiro;
	}
	public void setAmphiro(AmphiroModeState amphiro) {
		this.amphiro = amphiro;
	}

	public SocialModeState getSocial() {
		return social;
	}
	public void setSocial(SocialModeState social) {
		this.social = social;
	}

	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
