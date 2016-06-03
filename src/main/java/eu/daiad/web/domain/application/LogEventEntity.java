package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.logging.EnumLevel;

@Entity(name = "log4j_message")
@Table(schema = "public", name = "log4j_message")
public class LogEventEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "log4j_message_id_seq", name = "log4j_message_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "log4j_message_id_seq", strategy = GenerationType.SEQUENCE)
	private long id;

	@Basic()
	private String account;

	@Column(name = "remote_address")
	private String remoteAddress;

	@Basic()
	private String category;

	@Basic()
	private String code;

	@Enumerated(EnumType.STRING)
	private EnumLevel level;

	@Basic()
	private String logger;

	@Basic()
	private String message;

	@Column(name = "timestamp")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime timestamp;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public EnumLevel getLevel() {
		return level;
	}

	public void setLevel(EnumLevel level) {
		this.level = level;
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	public long getId() {
		return id;
	}

}
