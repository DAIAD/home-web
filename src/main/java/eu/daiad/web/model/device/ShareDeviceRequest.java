package eu.daiad.web.model.device;

import java.util.UUID;

import eu.daiad.web.model.AuthenticatedRequest;

public class ShareDeviceRequest extends AuthenticatedRequest {

	private UUID device;

	private String assignee;

	private boolean shared;

	public UUID getDevice() {
		return device;
	}

	public void setDevice(UUID device) {
		this.device = device;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

}
