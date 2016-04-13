package eu.daiad.web.model.loader;

import eu.daiad.web.model.spatial.ReferenceSystem;

public class ImportWaterMeterFileConfiguration {

	private String filename;

	private ReferenceSystem sourceReferenceSystem = new ReferenceSystem(4326);

	private ReferenceSystem targetReferenceSystem = new ReferenceSystem(4326);

	private int usernameCellIndex = 0;

	private int meterIdCellIndex = 1;

	private int longitudeCellIndex = 2;

	private int latitudeCellIndex = 3;

	public ImportWaterMeterFileConfiguration(String filename) {
		this.filename = filename;

	}

	public String getFilename() {
		return filename;
	}

	public int getUsernameCellIndex() {
		return usernameCellIndex;
	}

	public void setUsernameCellIndex(int usernameCellIndex) {
		this.usernameCellIndex = usernameCellIndex;
	}

	public int getMeterIdCellIndex() {
		return meterIdCellIndex;
	}

	public void setMeterIdCellIndex(int meterIdCellIndex) {
		this.meterIdCellIndex = meterIdCellIndex;
	}

	public int getLongitudeCellIndex() {
		return longitudeCellIndex;
	}

	public void setLongitudeCellIndex(int longitudeCellIndex) {
		this.longitudeCellIndex = longitudeCellIndex;
	}

	public int getLatitudeCellIndex() {
		return latitudeCellIndex;
	}

	public void setLatitudeCellIndex(int latitudeCellIndex) {
		this.latitudeCellIndex = latitudeCellIndex;
	}

	public ReferenceSystem getSourceReferenceSystem() {
		return sourceReferenceSystem;
	}

	public void setSourceReferenceSystem(ReferenceSystem sourceReferenceSystem) {
		this.sourceReferenceSystem = sourceReferenceSystem;
	}

	public ReferenceSystem getTargetReferenceSystem() {
		return targetReferenceSystem;
	}

	public void setTargetReferenceSystem(ReferenceSystem targetReferenceSystem) {
		this.targetReferenceSystem = targetReferenceSystem;
	}

}
