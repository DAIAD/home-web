package eu.daiad.web.model.message;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiverAccount {
    @JsonProperty
    private Integer id;

    @JsonProperty
    private UUID key;

    @JsonProperty
    private String username;

    private String firstName;

    private String lastName;

    private DateTime acknowledgedOn;

    public ReceiverAccount() {
    }

    public ReceiverAccount(int accountId, String username, String firstName, String lastName, DateTime acknowledgedOn) {
        id = accountId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.acknowledgedOn = acknowledgedOn;
    }

    public ReceiverAccount(UUID accountKey, String username, String firstName, String lastName, DateTime acknowledgedOn) {
        key = accountKey;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.acknowledgedOn = acknowledgedOn;
    }

    public static ReceiverAccount of(int accountId, String username, String firstName, String lastName) {
        return new ReceiverAccount(accountId, username, firstName, lastName, null);
    }

    public static ReceiverAccount of(UUID accountKey, String username, String firstName, String lastName) {
        return new ReceiverAccount(accountKey, username, firstName, lastName, null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer accountId) {
        id = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    // Backwards compatibility

    @JsonProperty("accountId")
    public Integer getAccountId() {
        return id;
    }

    @JsonProperty("accountId")
    public void setAccountId(Integer id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFullName() {
        if (StringUtils.isBlank(lastName)) {
            return (StringUtils.isBlank(firstName) ? "" : firstName);
        } else {
            return (StringUtils.isBlank(firstName) ? lastName : lastName + ", " + firstName);
        }
    }

    public DateTime getAcknowledgedOn() {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(DateTime acknowledgedOn) {
        this.acknowledgedOn = acknowledgedOn;
    }

}
