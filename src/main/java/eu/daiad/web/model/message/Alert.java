package eu.daiad.web.model.message;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.NumberFormatter;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Recommendation.ParameterizedTemplate;
import eu.daiad.web.service.ICurrencyRateService;

public class Alert extends Message
{
    public interface ParameterizedTemplate extends Message.Parameters
    {
        /**
         * Return the template to be used for this alert.
         */
        public EnumAlertTemplate getTemplate();
        
        /**
         * Convert any locale-sensitive parameters inside this template.
         *
         * @param target The target locale
         * @param currencyRate A service providing currency-rate information in case a
         *    parameter represents a money amounts (and must be converted) 
         * @return a localized {@link ParameterizedTemplate}
         */
        public ParameterizedTemplate withLocale(Locale target, ICurrencyRateService currencyRate);
    }
    
    /**
     * A simple and logic-less implementation for {@link ParameterizedTemplate} using anonymous
     * parameters. 
     */
    public static class SimpleParameterizedTemplate extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        @NotNull
        EnumAlertTemplate alertTemplate;

        // Provide some common parameters
        
        private Integer integer1;

        private Integer integer2;

        private BigDecimal money1;

        private BigDecimal money2;

        public SimpleParameterizedTemplate()
        {
            super();
        }

        public SimpleParameterizedTemplate(
            DateTime refDate, EnumDeviceType deviceType, EnumAlertTemplate alertTemplate)
        {
            super(refDate, deviceType);
            this.alertTemplate = alertTemplate;
        }

        @JsonProperty("integer1")
        public Integer getInteger1()
        {
            return integer1;
        }

        public SimpleParameterizedTemplate withInteger1(Integer n)
        {
            this.integer1 = n;
            return this;
        }

        @JsonProperty("integer1")
        public void setInteger1(Integer n)
        {
            this.integer1 = n;
        }
        
        @JsonProperty("integer2")
        public Integer getInteger2()
        {
            return integer2;
        }
        
        public SimpleParameterizedTemplate withInteger2(Integer integer2)
        {
            this.integer2 = integer2;
            return this;
        }
        
        @JsonProperty("integer2")
        public void setInteger2(Integer n)
        {
            this.integer2 = n;
        }
        
        @JsonProperty("money1")
        public BigDecimal getMoney1()
        {
            return money1;
        }

        public SimpleParameterizedTemplate withMoney1(BigDecimal y)
        {
            setMoney1(y);
            return this;
        }
        
        public SimpleParameterizedTemplate withMoney1(Double y)
        {
            setMoney1(y);
            return this;
        }
        
        @JsonProperty("money1")
        public void setMoney1(BigDecimal y)
        {
            this.money1 = y;
        }
        
        @JsonIgnore
        public void setMoney1(double y)
        {
            this.money1 = new BigDecimal(y);
        }
           
        @JsonProperty("money2")
        public BigDecimal getMoney2()
        {
            return money2;
        }

        public SimpleParameterizedTemplate withMoney2(BigDecimal y)
        {
            setMoney2(y);
            return this;
        }
        
        public SimpleParameterizedTemplate withMoney2(Double y)
        {
            setMoney2(y);
            return this;
        }       
        
        @JsonProperty("money2")
        public void setMoney2(BigDecimal y)
        {
            this.money2 = y;
        }
        
        @JsonIgnore
        public void setMoney2(double y)
        {
            this.money2 = new BigDecimal(y);
        }
        
        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> pairs = super.getParameters();

            if (integer1 != null)
                pairs.put("integer1", integer1);
            if (integer2 != null)
                pairs.put("integer2", integer2);

            if (money1 != null)
                pairs.put("money1", money1);
            if (money2 != null)
                pairs.put("money2", money2);

            return pairs;
        }

        @JsonProperty("template")
        public void setTemplate(EnumAlertTemplate template)
        {
            alertTemplate = template;
        }
        
        @JsonProperty("template")
        @Override
        public EnumAlertTemplate getTemplate()
        {
            return alertTemplate;
        }
        
        @Override
        public SimpleParameterizedTemplate withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            // Convert {money1, money2} using currency-rate service
            
            BigDecimal rate = currencyRate.getRate(Locale.getDefault(), target);
            
            if (money1 != null)
                money1 = money1.multiply(rate);
            
            if (money2 != null)
                money2 = money2.multiply(rate);
            
            return this;
        }    
    }

    protected int priority;

	private final EnumAlertType alertType;

    private final EnumAlertTemplate alertTemplate;

    private Long refDate;
    
	private String description;

	private String link;

	public Alert(int id, EnumAlertTemplate template)
    {
        super(id);
        this.alertTemplate = template;
        this.alertType = template.getType();
        this.priority = alertType.getPriority().intValue();
    }

    public Alert(int id, EnumAlertType type)
    {
        super(id);
        this.alertTemplate = null;
        this.alertType = type;
        this.priority = alertType.getPriority().intValue();
    }

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.ALERT;
	}

	public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public EnumAlertType getAlertType() {
		return alertType;
	}

	public EnumAlertTemplate getAlertTemplate() {
        return alertTemplate;
    }

    @Override
    public String getBody()
    {
        return title;
    }

    @JsonProperty("refDate")
    public Long getRefDate()
    {
        return refDate;
    }

    @JsonProperty("refDate")
    public void setRefDate(Long refDate)
    {
        this.refDate = refDate;
    }
    
    @JsonIgnore
    public void setRefDate(DateTime refDate)
    {
        this.refDate = refDate.getMillis();
    }
}
