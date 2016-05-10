package eu.daiad.web.model.message;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

public class MessageCalculationConfiguration {

	private int computeThisDayOfMonth = new DateTime().dayOfMonth().getMaximumValue();
	private int computeThisDayOfWeek = DateTimeConstants.SUNDAY;

	// send static tip every 7 days
	private int staticTipInterval = DateTimeConstants.DAYS_PER_WEEK;

	// compute aggregates every week
	private int aggregateComputationInterval = DateTimeConstants.DAYS_PER_WEEK;

	private String intKey1 = "integer1";
	private String intKey2 = "integer2";
	private String currencyKey1 = "currency1";
	private String currencyKey2 = "currency2";
	private double eurosPerKwh = 0.224;
	private double averageGbpPerKwh = 0.15;
	private double eurosPerLiter = 0.0024;

	private Integer dailyBudget = 50;
	private Integer weeklyBudget = 350;
	private Integer monthlyBudget = 1500;

	private Integer dailyBudgetAmphiro = 20;
	private Integer weeklyBudgetAmphiro = 140;
	private Integer monthlyBudgetAmphiro = 600;

	private Integer utilityId;

	private DateTimeZone timezone;

	public int getComputeThisDayOfMonth() {
		return computeThisDayOfMonth;
	}

	public void setComputeThisDayOfMonth(int computeThisDayOfMonth) {
		this.computeThisDayOfMonth = computeThisDayOfMonth;
	}

	public int getComputeThisDayOfWeek() {
		return computeThisDayOfWeek;
	}

	public void setComputeThisDayOfWeek(int computeThisDayOfWeek) {
		this.computeThisDayOfWeek = computeThisDayOfWeek;
	}

	public String getIntKey1() {
		return intKey1;
	}

	public void setIntKey1(String intKey1) {
		this.intKey1 = intKey1;
	}

	public String getIntKey2() {
		return intKey2;
	}

	public void setIntKey2(String intKey2) {
		this.intKey2 = intKey2;
	}

	public String getCurrencyKey1() {
		return currencyKey1;
	}

	public void setCurrencyKey1(String currencyKey1) {
		this.currencyKey1 = currencyKey1;
	}

	public String getCurrencyKey2() {
		return currencyKey2;
	}

	public void setCurrencyKey2(String currencyKey2) {
		this.currencyKey2 = currencyKey2;
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

	public int getAggregateComputationInterval() {
		return aggregateComputationInterval;
	}

	public void setAggregateComputationInterval(int aggregateComputationInterval) {
		this.aggregateComputationInterval = aggregateComputationInterval;
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

	public Integer getUtilityId() {
		return utilityId;
	}

	public void setUtilityId(Integer utilityId) {
		this.utilityId = utilityId;
	}

	public DateTimeZone getTimezone() {
		return timezone;
	}

	public void setTimezone(DateTimeZone timezone) {
		this.timezone = timezone;
	}

}
