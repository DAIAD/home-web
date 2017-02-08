package eu.daiad.web.domain.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert.ParameterizedTemplate;

@Entity(name = "account_alert")
@Table(schema = "public", name = "account_alert")
public class AccountAlertEntity
{
	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "account_alert_id_seq",
	    name = "account_alert_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "account_alert_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne()
	@JoinColumn(name = "account_id", nullable = false)
	@NotNull
	private AccountEntity account;

	@OneToOne(
        mappedBy = "alert",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @JoinColumn(name = "account_alert_id")
    private AccountAlertParametersEntity parameters;

	@ManyToOne()
    @JoinColumn(name = "alert_template", nullable = false)
	@NotNull
    private AlertTemplateEntity alertTemplate;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	@Column(name = "receive_acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime receiveAcknowledgedOn;
	
	@Column(name = "device_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private EnumDeviceType deviceType;
	
	@ManyToOne()
    @JoinColumn(name = "resolver_execution", nullable = false)
    @NotNull
	private AlertResolverExecutionEntity resolverExecution;
	
	public AccountAlertEntity() {}
	
	public AccountEntity getAccount()
	{
		return account;
	}

	public void setAccount(AccountEntity account)
	{
		this.account = account;
	}

	public AlertTemplateEntity getTemplate()
    {
        return alertTemplate;
    }
	
	public void setTemplate(AlertTemplateEntity alertTemplate)
    {
        this.alertTemplate = alertTemplate;
    }

    public DateTime getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn)
	{
		this.createdOn = createdOn;
	}

	public DateTime getAcknowledgedOn()
	{
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(DateTime acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}

	public int getId() {
		return id;
	}

	public DateTime getReceiveAcknowledgedOn()
	{
		return receiveAcknowledgedOn;
	}

	public void setReceiveAcknowledgedOn(DateTime receiveAcknowledgedOn)
	{
		this.receiveAcknowledgedOn = receiveAcknowledgedOn;
	}

    public EnumDeviceType getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(EnumDeviceType deviceType)
    {
        this.deviceType = deviceType;
    }

    public AlertResolverExecutionEntity getResolverExecution()
    {
        return resolverExecution;
    }
    
    public DateTime getRefDate()
    {
        return resolverExecution.getRefDate();
    }
    
    public String getResolverName()
    {
        return resolverExecution.getResolverName();
    }
    
    public void setResolverExecution(AlertResolverExecutionEntity resolverExecution)
    {
        this.resolverExecution = resolverExecution;
    }
    
    public AccountAlertParametersEntity getParameters()
    {
        return parameters;
    }

    public void setParameters(ParameterizedTemplate parameterizedTemplate)
    {
        if (parameterizedTemplate == null) {
            parameters = null;
        } else {
            try {
                parameters = new AccountAlertParametersEntity(this, parameterizedTemplate);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Failed to create parameters entity", ex);
            }
        }
    }
    
    
}
