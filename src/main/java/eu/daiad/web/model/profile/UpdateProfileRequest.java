package eu.daiad.web.model.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.EnumApplication;

public class UpdateProfileRequest extends AuthenticatedRequest {

    @JsonIgnore
    private EnumApplication application = EnumApplication.UNDEFINED;

    private Integer dailyMeterBudget;

    private Integer dailyAmphiroBudget;

    private String configuration;

    private String firstname;

    private String lastname;

    private String locale;

    private String timezone;

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public EnumApplication getApplication() {
        return application;
    }

    public void setApplication(EnumApplication application) {
        this.application = application;
    }

    public Integer getDailyMeterBudget() {
        return dailyMeterBudget;
    }

    public void setDailyMeterBudget(Integer dailyMeterBudget) {
        this.dailyMeterBudget = dailyMeterBudget;
    }

    public Integer getDailyAmphiroBudget() {
        return dailyAmphiroBudget;
    }

    public void setDailyAmphiroBudget(Integer dailyAmphiroBudget) {
        this.dailyAmphiroBudget = dailyAmphiroBudget;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

}
