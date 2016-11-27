package eu.daiad.web.model.message;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;

import eu.daiad.web.model.device.EnumDeviceType;

public class MessageCalculationConfiguration 
{    
	private LocalDateTime refDate;
    
    // send static tip every 7 days
	private int staticTipInterval = DateTimeConstants.DAYS_PER_WEEK;

	private String intKey1 = "integer1";
	private String intKey2 = "integer2";
	private String currencyKey1 = "currency1";
	private String currencyKey2 = "currency2";
	private String dayKey = "day";
	
	private Integer meterDailyBudget = 300;
	private Integer meterWeeklyBudget = 2100;
	private Integer meterMonthlyBudget = 9000;

	private Integer amphiroDailyBudget = 100;
	private Integer amphiroWeeklyBudget = 700;
	private Integer amphiroMonthlyBudget = 3000;
    
    private boolean onDemandExecution;

    public MessageCalculationConfiguration(LocalDateTime refDate)
    {
        this.refDate = refDate;
    }
    
    public MessageCalculationConfiguration()
    {
        this.refDate = LocalDateTime.now();
    }
    
    public LocalDateTime getRefDate()
    {
        return refDate;
    }
    
	public int getComputeThisDayOfMonth() {
		return new DateTime().dayOfMonth().getMaximumValue();
	}

	public int getComputeThisDayOfWeek() {
		return DateTimeConstants.SUNDAY;
	}

	public String getIntKey1() {
		return intKey1;
	}

	public String getIntKey2() {
		return intKey2;
	}

	public String getCurrencyKey1() {
		return currencyKey1;
	}

	public String getCurrencyKey2() {
		return currencyKey2;
	}

	public int getStaticTipInterval() {
		return staticTipInterval;
	}

	public void setStaticTipInterval(int staticTipInterval) {
		this.staticTipInterval = staticTipInterval;
	}

	public Integer getDailyBudget() {
		return meterDailyBudget;
	}
	
	public Integer getDailyBudget(EnumDeviceType deviceType) 
	{
        switch (deviceType) {
        case AMPHIRO:
            return amphiroDailyBudget;
        case METER:
        default:
            return meterDailyBudget;
        }
    }

	public void setDailyBudget(int budget) {
		this.meterDailyBudget = budget;
	}
	
	public void setDailyBudget(EnumDeviceType deviceType, int budget) 
	{
	    switch (deviceType) {
        case AMPHIRO:
            this.amphiroDailyBudget = budget;
            break;
        case METER:
        default:
            this.meterDailyBudget = budget;
            break;
        } 
    }
	
	public Integer getWeeklyBudget() {
	    return meterWeeklyBudget;
	}

	public Integer getWeeklyBudget(EnumDeviceType deviceType) 
	{
	    switch (deviceType) {
	    case AMPHIRO:
	        return amphiroWeeklyBudget;
	    case METER:
	    default:
	        return meterWeeklyBudget;
	    }
	}

	public void setWeeklyBudget(int budget) {
	    this.meterWeeklyBudget = budget;
	}

	public void setWeeklyBudget(EnumDeviceType deviceType, int budget) 
	{
	    switch (deviceType) {
	    case AMPHIRO:
	        this.amphiroWeeklyBudget = budget;
	        break;
	    case METER:
	    default:
	        this.meterWeeklyBudget = budget;
	        break;
	    } 
	}
	
	public Integer getMonthlyBudget() {
	    return meterMonthlyBudget;
	}

	public Integer getMonthlyBudget(EnumDeviceType deviceType) 
	{
	    switch (deviceType) {
	    case AMPHIRO:
	        return amphiroMonthlyBudget;
	    case METER:
	    default:
	        return meterMonthlyBudget;
	    }
	}

	public void setMonthlyBudget(int budget) {
	    this.meterMonthlyBudget = budget;
	}

	public void setMonthlyBudget(EnumDeviceType deviceType, int budget) 
	{
	    switch (deviceType) {
	    case AMPHIRO:
	        this.amphiroMonthlyBudget = budget;
	        break;
	    case METER:
	    default:
	        this.meterMonthlyBudget = budget;
	        break;
	    } 
	}

    public boolean isOnDemandExecution() {
        return onDemandExecution;
    }

    public void setOnDemandExecution(boolean onDemandExecution) {
        this.onDemandExecution = onDemandExecution;
    }
}
