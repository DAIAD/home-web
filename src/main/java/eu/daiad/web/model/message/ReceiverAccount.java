package eu.daiad.web.model.message;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiverAccount
{
    @JsonProperty
    private Integer id;

    @JsonProperty
    private UUID key;
    
    @JsonProperty
    private String username;

    public ReceiverAccount() {}

    public ReceiverAccount(int accountId, String username)
    {
        this.id = accountId;
        this.username = username;
    }

    public ReceiverAccount(UUID accountKey, String username)
    {
        this.key = accountKey;
        this.username = username;
    }
    
    public static ReceiverAccount of(int accountId, String username)
    {
        return new ReceiverAccount(accountId, username);
    }
    
    public static ReceiverAccount of(UUID accountKey, String username)
    {
        return new ReceiverAccount(accountKey, username);
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer accountId) {
        this.id = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getKey()
    {
        return key;
    }

    public void setKey(UUID key)
    {
        this.key = key;
    }
    
    // Backwards compatibility
     
    @JsonProperty("accountId")
    public Integer getAccountId()
    {
        return id;
    }
    
    @JsonProperty("accountId")
    public void setAccountId(Integer id)
    {
        this.id = id;
    }
}
