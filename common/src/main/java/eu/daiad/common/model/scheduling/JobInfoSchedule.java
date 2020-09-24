package eu.daiad.common.model.scheduling;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @Type(name = "PERIOD", value = JobInfoSchedulePeriodic.class),
    @Type(name = "CRON", value = JobInfoScheduleCron.class),
})
public abstract class JobInfoSchedule {

	@Getter
	protected EnumScheduleType type;
	
}
