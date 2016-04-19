package eu.daiad.web.model.loader;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class UploadRequest {

	@JsonDeserialize(using = EnumUploadFileType.Deserializer.class)
	private EnumUploadFileType type;

	private String timezone;

	private MultipartFile[] files;

	public EnumUploadFileType getType() {
		return type;
	}

	public void setType(EnumUploadFileType type) {
		this.type = type;
	}

	public MultipartFile[] getFiles() {
		return files;
	}

	public void setFiles(MultipartFile[] files) {
		this.files = files;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

}
