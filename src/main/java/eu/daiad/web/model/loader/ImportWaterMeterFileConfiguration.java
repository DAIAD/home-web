package eu.daiad.web.model.loader;

import eu.daiad.web.model.spatial.ReferenceSystem;

/**
 * Configuration for importing smart water meter data to HBASE.
 */
public class ImportWaterMeterFileConfiguration {

    /**
     * The path of the input file.
     */
	private String path;

	/**
	 * Initial file name.
	 */
	private String initialFilename;

	/**
	 * Source spatial reference system.
	 */
	private ReferenceSystem sourceReferenceSystem = new ReferenceSystem(4326);

	/**
	 * Target spatial reference system.
	 */
	private ReferenceSystem targetReferenceSystem = new ReferenceSystem(4326);

	/**
	 * The index of the column containing the user name.
	 */
	private int usernameCellIndex = 0;

	/**
	 * The index of the column containing the smart water meter unique serial number.
	 */
	private int meterIdCellIndex = 1;

	/**
	 * The index of the column containing the longitude value.
	 */
	private int longitudeCellIndex = 2;

	/**
	 * The index of the column containing the latitude value.
	 */
	private int latitudeCellIndex = 3;

	/**
	 * Indicates if the first row contains the column names and should be skipped.
	 */
	private boolean firstRowHeader = false;

	public ImportWaterMeterFileConfiguration(String path, String initialFilename) {
		this.path = path;
		this.initialFilename = initialFilename;
	}

	public String getPath() {
		return path;
	}

	public String getInitialFilename() {
	    return initialFilename;
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

	public boolean isFirstRowHeader() {
		return firstRowHeader;
	}

	public void setFirstRowHeader(boolean firstRowHeader) {
		this.firstRowHeader = firstRowHeader;
	}

}
