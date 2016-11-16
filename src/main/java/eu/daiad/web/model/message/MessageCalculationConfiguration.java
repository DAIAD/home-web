package eu.daiad.web.model.message;

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

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
	
	private double eurosPerKwh = 0.224;
	private double averageGbpPerKwh = 0.15;
	private double eurosPerLiter = 0.0024;

	private Integer dailyBudget = 300;
	private Integer weeklyBudget = 2100;
	private Integer monthlyBudget = 9000;

	private Integer dailyBudgetAmphiro = 100;
	private Integer weeklyBudgetAmphiro = 700;
	private Integer monthlyBudgetAmphiro = 3000;
    
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

	public double getEurosPerKwh() {
		return eurosPerKwh;
	}

	public void setEurosPerKwh(double eurosPerKwh) {
		this.eurosPerKwh = eurosPerKwh;
	}

	public double getAverageGbpPerKwh() {
		return averageGbpPerKwh;
	}

	public void setAverageGbpPerKwh(double averageGbpPerKwh) {
		this.averageGbpPerKwh = averageGbpPerKwh;
	}

	public double getEurosPerLiter() {
		return eurosPerLiter;
	}

	public void setEurosPerLiter(double eurosPerLiter) {
		this.eurosPerLiter = eurosPerLiter;
	}

	public Integer getDailyBudget() {
		return dailyBudget;
	}

	public void setDailyBudget(Integer dailyBudget) {
		this.dailyBudget = dailyBudget;
	}

	public Integer getWeeklyBudget() {
		return weeklyBudget;
	}

	public void setWeeklyBudget(Integer weeklyBudget) {
		this.weeklyBudget = weeklyBudget;
	}

	public Integer getMonthlyBudget() {
		return monthlyBudget;
	}

	public void setMonthlyBudget(Integer monthlyBudget) {
		this.monthlyBudget = monthlyBudget;
	}

	public Integer getDailyBudgetAmphiro() {
		return dailyBudgetAmphiro;
	}

	public void setDailyBudgetAmphiro(Integer dailyBudgetAmphiro) {
		this.dailyBudgetAmphiro = dailyBudgetAmphiro;
	}

	public Integer getWeeklyBudgetAmphiro() {
		return weeklyBudgetAmphiro;
	}

	public void setWeeklyBudgetAmphiro(Integer weeklyBudgetAmphiro) {
		this.weeklyBudgetAmphiro = weeklyBudgetAmphiro;
	}

	public Integer getMonthlyBudgetAmphiro() {
		return monthlyBudgetAmphiro;
	}

	public void setMonthlyBudgetAmphiro(Integer monthlyBudgetAmphiro) {
		this.monthlyBudgetAmphiro = monthlyBudgetAmphiro;
	}

    public boolean isOnDemandExecution() {
        return onDemandExecution;
    }

    public void setOnDemandExecution(boolean onDemandExecution) {
        this.onDemandExecution = onDemandExecution;
    }
}
