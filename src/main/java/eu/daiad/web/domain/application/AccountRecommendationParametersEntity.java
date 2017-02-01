package eu.daiad.web.domain.application;

import java.io.IOException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.model.message.Recommendation.ParameterizedTemplate;

@Entity(name = "account_recommendation_parameters")
@Table(schema = "public", name = "account_recommendation_parameters")
public class AccountRecommendationParametersEntity
{
    private static final ObjectMapper serializer = new ObjectMapper();
    
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "account_recommendation_parameters_id_seq",
        name = "account_recommendation_parameters_id_seq",
        allocationSize = 1,
        initialValue = 1)
    @GeneratedValue(generator = "account_recommendation_parameters_id_seq")
    private int id;
    
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
        name = "account_recommendation_id",
        foreignKey = @ForeignKey(name = "fk_account_recommendation_parameters"),
        nullable = false,
        unique = true,
        updatable = false
    )
    @NotNull
    private AccountRecommendationEntity recommendation;
    
    @Basic()
    @Column(name = "class_name", nullable = false)
    @NotNull
    private String className;

    @Basic()
    @Column(name = "json_data", nullable = false)
    @NotNull
    private String jsonData;
    
    public AccountRecommendationParametersEntity() {}
    
    public AccountRecommendationParametersEntity(
        AccountRecommendationEntity recommendation, ParameterizedTemplate parameterizedTemplate) 
        throws JsonProcessingException 
    {
        this.recommendation = recommendation;
        
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
    
    public AccountRecommendationEntity getRecommendation()
    {
        return recommendation;
    }
    
    public ParameterizedTemplate toParameterizedTemplate() 
        throws ClassNotFoundException, ClassCastException, IOException
    {
        Class<? extends ParameterizedTemplate> cls = Class.forName(className)
            .asSubclass(ParameterizedTemplate.class);
        
        return serializer.readValue(jsonData, cls);
    }
}
