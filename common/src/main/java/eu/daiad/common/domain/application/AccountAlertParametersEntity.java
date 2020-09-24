package eu.daiad.common.domain.application;

import java.io.IOException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.common.model.message.Alert.ParameterizedTemplate;

@Entity(name = "account_alert_parameters")
@Table(schema = "public", name = "account_alert_parameters")
public class AccountAlertParametersEntity
{
    private static final ObjectMapper serializer = new ObjectMapper();
    
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "account_alert_parameters_id_seq",
        name = "account_alert_parameters_id_seq",
        allocationSize = 1,
        initialValue = 1)
    @GeneratedValue(generator = "account_alert_parameters_id_seq")
    private int id;
    
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
        name = "account_alert_id",
        foreignKey = @ForeignKey(name = "fk_account_alert_parameters"),
        nullable = false,
        unique = true,
        updatable = false
    )
    @NotNull
    private AccountAlertEntity alert;
    
    @Basic()
    @Column(name = "class_name", nullable = false)
    @NotNull
    private String className;

    @Basic()
    @Column(name = "json_data", nullable = false)
    @NotNull
    private String jsonData;
    
    public AccountAlertParametersEntity() {}
    
    public AccountAlertParametersEntity(
        AccountAlertEntity alert, ParameterizedTemplate parameterizedTemplate) 
        throws JsonProcessingException 
    {
        this.alert = alert;
        
        // Serialize parameterized template
        
        this.className = parameterizedTemplate.getClass().getName();
        this.jsonData = serializer.writeValueAsString(parameterizedTemplate);
    }
    public int getId()
    {
        return id;
    }

    public String getJsonData()
    {
        return jsonData;
    }
    
    public AccountAlertEntity getAlert()
    {
        return alert;
    }
    
    public ParameterizedTemplate toParameterizedTemplate() 
        throws ClassNotFoundException, ClassCastException, IOException
    {
        Class<? extends ParameterizedTemplate> cls = Class.forName(className)
            .asSubclass(ParameterizedTemplate.class);
        
        return serializer.readValue(jsonData, cls);
    }
}
