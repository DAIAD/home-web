package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.UUID;

public class DeviceAmphiroConfiguration {

	UUID version;

	private String title;

	private long createdOn;

	private Long acknowledgedOn;

	private Long enabledOn;

	private ArrayList<Integer> properties = new ArrayList<Integer>();

	private int block;

	private int numberOfFrames;

	private int frameDuration;

	public ArrayList<Integer> getProperties() {
		return properties;
	}

	public void add(int value) {
		this.properties.add(value);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public int getNumberOfFrames() {
		return numberOfFrames;
	}

	public void setNumberOfFrames(int numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
	}

	public int getFrameDuration() {
		return frameDuration;
	}

	public void setFrameDuration(int frameDuration) {
		this.frameDuration = frameDuration;
	}

	public void setProperties(ArrayList<Integer> properties) {
		this.properties = properties;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

	public UUID getVersion() {
		return version;
	}

	public void setVersion(UUID version) {
		this.version = version;
	}

	public Long getAcknowledgedOn() {
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(Long acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}

	public Long getEnabledOn() {
		return enabledOn;
	}

	public void setEnabledOn(Long enabledOn) {
		this.enabledOn = enabledOn;
	}

}
