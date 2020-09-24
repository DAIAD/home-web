package eu.daiad.common.model.loader;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class UploadRequest {

	private MultipartFile[] files;

	@JsonDeserialize(using = EnumUploadFileType.Deserializer.class)
	private EnumUploadFileType type;

	private boolean firstRowHeader;

	private String timezone;

	private Integer srid;

	public Integer getSrid() {
		return srid;
	}

	public void setSrid(Integer srid) {
		this.srid = srid;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public MultipartFile[] getFiles() {
		return files;
	}

	public void setFiles(MultipartFile[] files) {
		this.files = files;
	}

	public EnumUploadFileType getType() {
		return type;
	}

	public void setType(EnumUploadFileType type) {
		this.type = type;
	}

	public boolean isFirstRowHeader() {
		return firstRowHeader;
	}

	public void setFirstRowHeader(boolean firstRowHeader) {
		this.firstRowHeader = firstRowHeader;
	}

}
