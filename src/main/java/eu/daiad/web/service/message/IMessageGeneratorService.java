package eu.daiad.web.service.message;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;

import eu.daiad.web.model.device.EnumDeviceType;

public interface IMessageGeneratorService {

	/**
	 * Generates messages i.e. alerts, recommendations and tips for all users of all utilities based on
	 * a set of configuration options.
	 *
	 * @param config the configuration options
	 */
	public abstract void executeAll(Configuration config);

	/**
	 * Generates messages i.e. alerts, recommendations and tips for all users of a utility based on the utility
	 * key and a set of configuration options.
	 *
	 * @param config the configuration options
	 */
	public abstract void executeUtility(Configuration config, UUID utilityKey);

	/**
	 * Generates messages i.e. alerts, recommendations and tips for a single user based on its key, its utility
	 * key and a set of configuration options.
	 *
	 * @param config the configuration options
	 */
	public abstract void executeAccount(Configuration config, UUID utilityKey, UUID accountKey);

	//
	// Configuration class
	//

	public static class Configuration
	{
	    private LocalDateTime refDate;

	    // send static tip every 7 days
	    private int tipInterval = DateTimeConstants.DAYS_PER_WEEK;

	    // Todo: Eliminate those keys; each template introduces own parameters
	    //private String intKey1 = "integer1";
	    //private String intKey2 = "integer2";
	    //private String currencyKey1 = "currency1";
	    //private String currencyKey2 = "currency2";
	    //private String dayKey = "day";

	    private Integer meterDailyBudget = 300;
	    private Integer meterWeeklyBudget = 2100;
	    private Integer meterMonthlyBudget = 9000;

	    private Integer amphiroDailyBudget = 100;
	    private Integer amphiroWeeklyBudget = 700;
	    private Integer amphiroMonthlyBudget = 3000;

	    private boolean onDemandExecution;

	    public Configuration(LocalDateTime refDate)
	    {
	        this.refDate = refDate;
	    }

	    public Configuration()
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

	    public int getTipInterval() {
	        return tipInterval;
	    }

	    public void setTipInterval(int days) {
	        this.tipInterval = days;
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
}
