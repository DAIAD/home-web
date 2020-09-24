package eu.daiad.common.service.mail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

public class Message {

	@Getter
	private String locale;

	@NotNull
	@Getter
	private EmailAddress sender;

	@NotEmpty
	@Getter
	private List<EmailAddress> recipients;

	@Getter
	@Setter
	private String Subject;

	@NotEmpty
	@Getter
	@Setter
	private String template;

	public Message(String locale) {
		this.locale = locale;
	}

	@Getter
	private final Map<String, Object> variables = new HashMap<>();

	@JsonProperty
	public void setSender(EmailAddress sender) {
		this.sender = sender;
	}

	public void setSender(String address) {
		this.sender = new EmailAddress(address);
	}

	public void setSender(String address, String name) {
		this.sender = new EmailAddress(address, name);
	}

	@JsonProperty
	public void setRecipients(List<EmailAddress> recipients) {
		this.recipients = recipients;
	}

	public void setRecipients(String address) {
		this.recipients = Arrays.asList(new EmailAddress(address));
	}

	public void setRecipients(String address, String name) {
		this.recipients = Arrays.asList(new EmailAddress(address, name));
	}

	@JsonProperty
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	@JsonIgnore
	public void setLocale(Locale locale) {
		this.locale = locale.toString();
	}

	public void setVariable(String key, Object value) {
		if (this.variables.containsKey(key)) {
			throw new IllegalArgumentException(String.format("Key {0} already exists", key));
		}

		this.variables.put(key, value);
	}

}
